package com.example.soundscape_app.service.playlist;

import com.example.soundscape_app.dto.ai.SmartPlaylistAiResult;
import com.example.soundscape_app.dto.ai.SmartPlaylistRequest;
import com.example.soundscape_app.dto.ai.SmartPlaylistResponse;
import com.example.soundscape_app.dto.response.song.PlaylistResponse;
import com.example.soundscape_app.dto.response.song.PlaylistWithListSongResponse;
import com.example.soundscape_app.dto.response.song.SongAndArtistResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.playlist.Playlist;
import com.example.soundscape_app.entity.playlist.PlaylistItem;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.enums.GenreEnum;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.mapper.song.PlaylistMapper;
import com.example.soundscape_app.mapper.song.SongMapper;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.repository.playlist.PlayListRepository;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.song.ListeningHistoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiPlaylistService {
    private static final int DEFAULT_DURATION_MINUTES = 45;
    private static final int MIN_DURATION_MINUTES = 15;
    private static final int MAX_DURATION_MINUTES = 180;
    private static final int MAX_PLAYLIST_SONGS = 20;
    private static final int CANDIDATE_LIMIT = 80;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final SongRepository songRepository;
    private final PlayListRepository playListRepository;
    private final PlaylistMapper playlistMapper;
    private final SongMapper songMapper;
    private final UserMapper userMapper;
    private final ListeningHistoryService listeningHistoryService;

    @Value("${spring.ai.openai.api-key:}")
    private String geminiApiKey;

    @Value("${spring.ai.openai.base-url:https://generativelanguage.googleapis.com}")
    private String geminiBaseUrl;

    @Value("${spring.ai.openai.chat.completions-path:/v1beta/openai/chat/completions}")
    private String geminiCompletionsPath;

    @Value("${spring.ai.openai.chat.options.model:gemini-2.5-flash}")
    private String geminiModel;

    @Transactional
    public SmartPlaylistResponse createSmartPlaylist(String authorizationHeader, SmartPlaylistRequest request) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        SmartPlaylistAiResult aiResult = askAi(request.prompt());
        List<Song> songs = pickSongs(aiResult);

        if (songs.isEmpty()) {
            throw new IllegalArgumentException("No suitable songs found for this AI playlist prompt");
        }

        Playlist playlist = new Playlist();
        playlist.setName(resolvePlaylistName(aiResult, request.prompt()));
        playlist.setImageUrl(songs.get(0).getImageUrl());
        playlist.setAuth(user);

        for (Song song : songs) {
            PlaylistItem item = new PlaylistItem();
            item.setPlaylist(playlist);
            item.setSong(song);
            playlist.getPlayListItem().add(item);
        }

        Playlist savedPlaylist = playListRepository.save(playlist);
        return new SmartPlaylistResponse(aiResult, toPlaylistWithSongs(savedPlaylist));
    }

    private SmartPlaylistAiResult askAi(String userPrompt) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        String systemPrompt = """
                        You convert a user's Vietnamese or English music mood into playlist search criteria.
                        Return only compact JSON, no markdown, no explanation.
                        JSON shape:
                        {"playlistName":"string","keyword":"string","genre":"GENRE","durationMinutes":45}
                        genre must be one of: %s.
                        Prefer LOFI, CHILL, AMBIENT, ACOUSTIC or INSTRUMENTAL for coding/focus/evening prompts.
                        durationMinutes must be between %d and %d.
                        """.formatted(availableGenres(), MIN_DURATION_MINUTES, MAX_DURATION_MINUTES);
        String response;
        if (chatModel != null) {
            response = chatModel.call(new SystemMessage(systemPrompt), new UserMessage(userPrompt));
        } else {
            response = callGeminiDirect(systemPrompt, userPrompt);
        }

        return parseAiResult(response, userPrompt);
    }

    private String callGeminiDirect(String systemPrompt, String userPrompt) {
        if (!notBlank(geminiApiKey)) {
            throw new IllegalStateException("GEMINI_API_KEY is required before using AI Playlist");
        }

        Map<String, Object> request = Map.of(
                "model", geminiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );

        JsonNode response = restClientBuilder
                .baseUrl(geminiBaseUrl)
                .build()
                .post()
                .uri(geminiCompletionsPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + geminiApiKey)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Gemini returned an empty response");
        }

        return response.path("choices").path(0).path("message").path("content").asText();
    }

    private SmartPlaylistAiResult parseAiResult(String response, String userPrompt) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(response));
            String playlistName = text(root, "playlistName");
            String keyword = text(root, "keyword");
            String genre = normalizeGenre(text(root, "genre"), userPrompt);
            Integer durationMinutes = normalizeDuration(root.path("durationMinutes").isNumber()
                    ? root.path("durationMinutes").asInt()
                    : DEFAULT_DURATION_MINUTES);

            return new SmartPlaylistAiResult(
                    blankToDefault(playlistName, "AI Playlist"),
                    blankToDefault(keyword, userPrompt),
                    genre,
                    durationMinutes
            );
        } catch (Exception ignored) {
            return heuristicResult(userPrompt);
        }
    }

    private List<Song> pickSongs(SmartPlaylistAiResult aiResult) {
        List<Song> candidates = new ArrayList<>(songRepository.findSmartPlaylistCandidates(
                safe(aiResult.keyword()),
                safe(aiResult.genre()),
                CANDIDATE_LIMIT
        ));

        if (candidates.size() < 5 && notBlank(aiResult.genre())) {
            candidates.addAll(songRepository.findSmartPlaylistCandidates("", aiResult.genre(), CANDIDATE_LIMIT));
        }
        if (candidates.size() < 5 && notBlank(aiResult.keyword())) {
            candidates.addAll(songRepository.findSmartPlaylistCandidates(aiResult.keyword(), "", CANDIDATE_LIMIT));
        }
        if (candidates.size() < 5) {
            candidates.addAll(songRepository.findSmartPlaylistCandidates("", "", CANDIDATE_LIMIT));
        }

        Map<Long, Song> uniqueById = new LinkedHashMap<>();
        candidates.forEach(song -> uniqueById.putIfAbsent(song.getId(), song));

        List<Song> uniqueActiveSongs = uniqueById.values().stream()
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .toList();

        return trimToDuration(uniqueActiveSongs, aiResult.durationMinutes());
    }

    private List<Song> trimToDuration(List<Song> songs, Integer durationMinutes) {
        int targetSeconds = normalizeDuration(durationMinutes) * 60;
        List<Song> selected = new ArrayList<>();
        double totalSeconds = 0;

        for (Song song : songs) {
            if (selected.size() >= MAX_PLAYLIST_SONGS) {
                break;
            }
            selected.add(song);
            totalSeconds += song.getDuration() != null && song.getDuration() > 0 ? song.getDuration() : 210;
            if (selected.size() >= 5 && totalSeconds >= targetSeconds) {
                break;
            }
        }

        return selected;
    }

    private PlaylistWithListSongResponse toPlaylistWithSongs(Playlist playlist) {
        PlaylistResponse playlistResponse = playlistMapper.toResponse(playlist);
        List<SongAndArtistResponse> songs = playlist.getPlayListItem().stream()
                .map(PlaylistItem::getSong)
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .map(song -> {
                    SongAndArtistResponse response = new SongAndArtistResponse();
                    SongResponse songResponse = songMapper.toSongResponse(song);
                    Auth artist = song.getAuth();
                    ArtistResponse artistResponse = userMapper.toArtistResponse(artist);
                    artistResponse.setMonthlyListeners(
                            listeningHistoryService.calculateAverageMonthlyListeners(artist.getId()));
                    response.setSong(songResponse);
                    response.setArtist(artistResponse);
                    return response;
                })
                .collect(Collectors.toList());

        return new PlaylistWithListSongResponse(playlistResponse, songs);
    }

    private SmartPlaylistAiResult heuristicResult(String userPrompt) {
        String normalized = userPrompt.toLowerCase(Locale.ROOT);
        String genre = normalizeGenre("", userPrompt);
        String playlistName = normalized.contains("code") || normalized.contains("java")
                ? "Nhạc chill để code"
                : "AI Playlist";
        return new SmartPlaylistAiResult(playlistName, userPrompt, genre, DEFAULT_DURATION_MINUTES);
    }

    private String normalizeGenre(String rawGenre, String userPrompt) {
        String candidate = safe(rawGenre).toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (isKnownGenre(candidate)) {
            return candidate;
        }

        String prompt = userPrompt.toLowerCase(Locale.ROOT);
        if (prompt.contains("lofi") || prompt.contains("lo-fi") || prompt.contains("code")
                || prompt.contains("java") || prompt.contains("focus")) {
            return GenreEnum.LOFI.name();
        }
        if (prompt.contains("chill") || prompt.contains("thư giãn") || prompt.contains("buổi tối")
                || prompt.contains("evening") || prompt.contains("night")) {
            return GenreEnum.CHILL.name();
        }
        if (prompt.contains("jazz")) {
            return GenreEnum.JAZZ.name();
        }
        if (prompt.contains("edm") || prompt.contains("dance")) {
            return GenreEnum.EDM.name();
        }
        if (prompt.contains("rock")) {
            return GenreEnum.ROCK.name();
        }
        if (prompt.contains("pop")) {
            return GenreEnum.POP.name();
        }
        return "";
    }

    private boolean isKnownGenre(String genre) {
        return Arrays.stream(GenreEnum.values()).anyMatch(value -> value.name().equals(genre));
    }

    private String availableGenres() {
        return Arrays.stream(GenreEnum.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private String extractJson(String response) {
        String cleaned = safe(response).trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String resolvePlaylistName(SmartPlaylistAiResult aiResult, String prompt) {
        String name = blankToDefault(aiResult.playlistName(), "AI Playlist");
        if (name.length() > 80) {
            name = name.substring(0, 80).trim();
        }
        if ("AI Playlist".equals(name) && notBlank(prompt)) {
            return prompt.length() > 80 ? prompt.substring(0, 80).trim() : prompt.trim();
        }
        return name;
    }

    private Integer normalizeDuration(Integer durationMinutes) {
        int value = durationMinutes == null ? DEFAULT_DURATION_MINUTES : durationMinutes;
        return Math.max(MIN_DURATION_MINUTES, Math.min(MAX_DURATION_MINUTES, value));
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : "";
    }

    private String blankToDefault(String value, String defaultValue) {
        return notBlank(value) ? value.trim() : defaultValue;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
