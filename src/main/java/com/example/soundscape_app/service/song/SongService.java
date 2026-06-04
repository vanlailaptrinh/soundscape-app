package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.request.song.SongRequest;
import com.example.soundscape_app.dto.response.song.ListSongResponse;
import com.example.soundscape_app.dto.response.song.DailyListeningTime;
import com.example.soundscape_app.dto.response.song.ListeningHistoryResponse;
import com.example.soundscape_app.dto.response.song.SongDetailResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.song.SongTrendingResponse;
import com.example.soundscape_app.dto.response.song.SongWithArtistResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.album.AlbumItem;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.mapper.song.SongMapper;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.common.AlbumValid;
import com.example.soundscape_app.util.FileUtil;
import com.example.soundscape_app.util.S3Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {
    private final S3Util s3Util;
    private final SongRepository songRepository;
    private final GenreService genreService;
    private final SongMapper songMapper;
    private final ListeningHistoryService listeningHistoryService;
    private final AuthService authService;
    private final AlbumValid albumValid;

    private void addAlbumItemToSongAndAlbum(Song song, Album album) {
        AlbumItem item = new AlbumItem();
        item.setSong(song);
        item.setAlbum(album);
        song.getAlbumItems().add(item);
        album.getAlbumItems().add(item);
    }

    private Song createAndSaveSong(SongRequest songRequest,
            Auth auth,
            Album album,
            String urlImage,
            String urlMedia,
            Map<String, Object> audioFeatures) {

        Song song = Song.builder()
                .title(songRequest.getTitle())
                .author(songRequest.getAuthor())
                .mediaUrl(urlMedia)
                .imageUrl(urlImage)
                .auth(auth)
                .type(FileUtil.getMediaType(songRequest.getFileMedia()))
                .genres(genreService.getGenresFromIds(songRequest.getGenreIds()))
                .albumItems(new ArrayList<>())
                .duration(asDouble(audioFeatures.get("duration")))
                .tempo(asDouble(audioFeatures.get("tempo")))
                .energy(asDouble(audioFeatures.get("energy")))
                .loudness(asDouble(audioFeatures.get("loudness")))
                .danceability(asDouble(audioFeatures.get("danceability")))
                .acousticness(asDouble(audioFeatures.get("acousticness")))
                .instrumentalness(asDouble(audioFeatures.get("instrumentalness")))
                .liveness(asDouble(audioFeatures.get("liveness")))
                .speechiness(asDouble(audioFeatures.get("speechiness")))
                .valence(asDouble(audioFeatures.get("valence")))
                .status(SongStatusEnum.ACTIVE)
                .build();

        if (album != null) {
            addAlbumItemToSongAndAlbum(song, album);
        }

        return songRepository.save(song);
    }

    private Double asDouble(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> analyzeMediaWithFlask(MultipartFile file) throws Exception {
        Map<String, Object> dummyData = new HashMap<>();
        dummyData.put("duration", 215.0);
        dummyData.put("tempo", 120.0);
        dummyData.put("energy", 0.8);
        dummyData.put("loudness", -5.0);
        dummyData.put("danceability", 0.7);
        dummyData.put("acousticness", 0.1);
        dummyData.put("instrumentalness", 0.0);
        dummyData.put("liveness", 0.2);
        dummyData.put("speechiness", 0.05);
        dummyData.put("valence", 0.6);
        return dummyData;
        // String flaskUrl = "http://localhost:5001/analyze";
        // RestTemplate restTemplate = new RestTemplate();

        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // // Tạo resource từ file gửi đi
        // ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
        // @Override
        // public String getFilename() {
        // return file.getOriginalFilename(); // Rất quan trọng! Flask cần tên file để
        // nhận đúng
        // }
        // };

        // MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // body.add("file", resource);

        // HttpEntity<MultiValueMap<String, Object>> requestEntity = new
        // HttpEntity<>(body, headers);

        // ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl,
        // requestEntity, Map.class);

        // if (response.getStatusCode().is2xxSuccessful()) {
        // return response.getBody();
        // } else {
        // throw new RuntimeException("Flask analyze API error: " +
        // response.getStatusCode());
        // }
    }

    private boolean isBannedSong(Long songId) {
        Song song = findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));
        return song.getStatus() == SongStatusEnum.BANNED;
    }

    @Transactional
    public SongResponse uploadSong(SongRequest songRequest, String authorizationHeader) throws Exception {

        MultipartFile file = songRequest.getFileMedia();
        FileUtil.validateAudioOrVideoFileOrThrow(file);

        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Album album = albumValid.validateAndGetAlbumIfNeeded(songRequest.getAlbumId(), auth);

        String urlImage = s3Util.uploadImageIfPresent(songRequest.getFileImage());
        String urlMedia = s3Util.uploadFile(file);

        Map<String, Object> audioFeatures = analyzeMediaWithFlask(file);

        Song song = createAndSaveSong(songRequest, auth, album, urlImage, urlMedia, audioFeatures);
        return songMapper.toSongResponse(song);
    }

    @Transactional
    public void listenSong(String authorizationHeader, Long songId) {
        if (isBannedSong(songId))
            throw new RuntimeException("This song is banned.");

        Song song = songRepository.findById(songId).orElse(null);
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        if (song == null || user == null)
            return;
        listeningHistoryService.addListeningHistory(user, song);
    }

    public void calDurationSong(String authorizationHeader, Long songId) {
        if (isBannedSong(songId))
            return;

        Song song = songRepository.findById(songId).orElse(null);
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);

        if (song == null || auth == null)
            return;
        long delta = listeningHistoryService.updateListeningDuration(auth, song);
        if (delta > song.getDuration() / 2) {
            song.setPlayCount(song.getPlayCount() + 1);
            songRepository.save(song);
        }
    }

    public Page<SongTrendingResponse> getTrendingSongs(Pageable pageable, double tau) {
        Page<SongTrendingResponse> allTrendingSongs = songRepository.findTrendingSongs(tau, pageable);

        // Filter out banned songs
        List<SongTrendingResponse> activeSongs = allTrendingSongs.getContent().stream()
                .filter(song -> {
                    Song fullSong = songRepository.findById(song.getId()).orElse(null);
                    return fullSong != null && fullSong.getStatus() != SongStatusEnum.BANNED;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(activeSongs, pageable, activeSongs.size());
    }

    public boolean isSongInAnyAlbum(Long songId) {
        if (isBannedSong(songId))
            return false;

        Song song = songRepository.findById(songId).orElse(null);
        if (song == null)
            return false;

        return song.getAlbumItems() != null && !song.getAlbumItems().isEmpty();
    }

    public Song getSongById(Long songId) {
        if (songId == null) {
            return null;
        }

        Song song = songRepository.findById(songId).orElse(null);
        if (song == null || song.getStatus() == SongStatusEnum.BANNED) {
            return null;
        }

        return song;
    }

    public SongResponse getSongResponseById(Long songId) {
        Song song = getSongById(songId);
        if (song == null)
            return null;
        return songMapper.toSongResponse(song);
    }

    public List<SongResponse> getSongsByArtistId(Long artistId) {
        List<Song> songs = songRepository.findByAuthId(artistId);

        // Filter out banned songs
        return songs.stream()
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
    }

    public Page<ListeningHistoryResponse> getUniqueListeningHistory(String authorizationHeader, Pageable pageable) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Page<ListeningHistoryResponse> historyPage = songRepository.findUniqueListeningHistory(auth.getId(), pageable);

        // Filter out banned songs
        List<ListeningHistoryResponse> activeHistory = historyPage.getContent().stream()
                .filter(history -> {
                    Song song = songRepository.findById(history.getId()).orElse(null);
                    return song != null && song.getStatus() != SongStatusEnum.BANNED;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(activeHistory, pageable, activeHistory.size());
    }

    public List<DailyListeningTime> getUserDailyListeningTime(String authorizationHeader, int days) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        return listeningHistoryService.getUserDailyListeningTime(auth, days);
    }

    public Page<SongTrendingResponse> getListSongsRecommend(String authorizationHeader, Pageable pageable) {
        try {
            Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
            Long userId = auth.getId();

            String flaskUrl = "http://127.0.0.1:5001/recommend/hybrid?user_id=" + userId;
            RestTemplate restTemplate = new RestTemplate();

            // --- Gọi Flask API ---
            ResponseEntity<Map> response = restTemplate.getForEntity(flaskUrl, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("recommendations")) {
                return Page.empty(pageable);
            }

            List<Integer> recommendedIdsInt = (List<Integer>) body.get("recommendations");
            if (recommendedIdsInt == null || recommendedIdsInt.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Long> recommendedIds = recommendedIdsInt.stream()
                    .map(Integer::longValue)
                    .toList();

            List<Song> recommendedSongs = songRepository.findAllById(recommendedIds);

            // Filter out banned songs
            recommendedSongs = recommendedSongs.stream()
                    .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                    .collect(Collectors.toList());

            Map<Long, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < recommendedIds.size(); i++) {
                orderMap.put(recommendedIds.get(i), i);
            }
            recommendedSongs.sort(Comparator.comparingInt(song -> orderMap.getOrDefault(song.getId(), Integer.MAX_VALUE)));

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), recommendedSongs.size());
            if (start > end) {
                return Page.empty(pageable);
            }

            List<Song> pagedSongs = recommendedSongs.subList(start, end);

            // --- Map sang DTO SongTrendingResponse có cả thông tin nghệ sĩ ---
            List<SongTrendingResponse> songResponses = pagedSongs.stream()
                    .<SongTrendingResponse>map(song -> new SongTrendingResponse() {
                        @Override
                        public Long getId() { return song.getId(); }
                        @Override
                        public String getTitle() { return song.getTitle(); }
                        @Override
                        public String getImageUrl() { return song.getImageUrl(); }
                        @Override
                        public String getAuthor() { return song.getAuthor(); }
                        @Override
                        public Long getArtistId() { return song.getAuth() != null ? song.getAuth().getId() : null; }
                        @Override
                        public String getUsername() { return song.getAuth() != null ? song.getAuth().getUsername() : null; }
                    })
                    .toList();

            return new PageImpl<>(songResponses, pageable, recommendedSongs.size());

        } catch (Exception e) {
            // Flask không chạy hoặc lỗi → trả về empty (FE sẽ fallback sang trending)
            System.out.println("[Recommend] Flask unavailable, returning empty: " + e.getMessage());
            return Page.empty(pageable);
        }
    }

    public Page<SongTrendingResponse> getRecentSongs(Pageable pageable) {
        return songRepository.findRecentSongs(pageable);
    }

    public List<SongResponse> getMySongs(String authorizationHeader) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        List<Song> songs = songRepository.findByAuthId(auth.getId());

        // Filter out banned songs
        return songs.stream()
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
    }

    public Long getTotalSongsByUser(Long userId) {
        List<Song> allSongs = songRepository.findByAuthId(userId);

        // Count only active songs
        return allSongs.stream()
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .count();
    }

    public SongWithArtistResponse getSongWithArtist(String authorizationHeader, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));

        if (song.getStatus() == SongStatusEnum.BANNED) {
            throw new RuntimeException("Song is not available");
        }

        // Record listening history if user is logged in
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            try {
                this.listenSong(authorizationHeader, songId);
            } catch (Exception e) {
                // Log or ignore to prevent blocking song play
            }
        }

        SongResponse songResponse = songMapper.toSongResponse(song);
        // MapStruct maps id as String – cast safely
        songResponse.setId(String.valueOf(song.getId()));

        Auth auth = song.getAuth();
        long totalPlays = songRepository.findByAuthId(auth.getId()).stream()
                .mapToLong(s -> s.getPlayCount())
                .sum();

        ArtistResponse artistResponse = new ArtistResponse(
                auth.getId(),
                auth.getUsername(),
                auth.getUrlAvatar(),
                totalPlays
        );

        return new SongWithArtistResponse(songResponse, artistResponse);
    }

    // ========== ADMIN METHODS - NO FILTER ==========

    public Page<ListSongResponse> getAllSongs(Pageable pageable) {
        return songRepository.findAll(pageable)
                .map(songMapper::toListSongResponse);
    }

    public Optional<Song> findById(Long songId) {
        return songRepository.findById(songId);
    }

    public SongDetailResponse getSongDetail(Long songId) {
        Song song = findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));
        return songMapper.toSongDetailResponse(song);
    }

    private String updateSongStatus(Long songId, SongStatusEnum status) {
        Song song = findById(songId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        song.setStatus(status);
        songRepository.save(song);

        return "User " + song.getTitle() + " status updated to " + status;
    }

    public String bannedSong(Long songId) {
        return updateSongStatus(songId, SongStatusEnum.BANNED);
    }

    public String unblockSong(Long songId) {
        return updateSongStatus(songId, SongStatusEnum.ACTIVE);
    }

}
