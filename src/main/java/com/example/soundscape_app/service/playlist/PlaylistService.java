package com.example.soundscape_app.service.playlist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.response.song.PlaylistResponse;
import com.example.soundscape_app.dto.response.song.PlaylistWithListSongResponse;
import com.example.soundscape_app.dto.response.song.SongAndArtistResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.playlist.Playlist;
import com.example.soundscape_app.entity.playlist.PlaylistItem;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.mapper.song.PlaylistMapper;
import com.example.soundscape_app.mapper.song.SongMapper;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.repository.playlist.PlayListRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.song.ListeningHistoryService;
import com.example.soundscape_app.service.song.SongService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlayListRepository playListRepository;
    private final SongService songService;
    private final AuthService authService;
    private final PlaylistMapper playlistMapper;
    private final SongMapper songMapper;
    private final ListeningHistoryService listeningHistoryService;
    private final UserMapper userMapper;

    public String addSongToPlaylist(String authorizationHeader, Long playlistId, Long songId) {
        Playlist playlist = playListRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        if (!playlist.getAuth().getId().equals(user.getId())) {
            return "You do not have permission to modify this playlist";
        }

        boolean exists = playlist.getPlayListItem().stream()
                .anyMatch(item -> item.getSong().getId().equals(songId));
        if (exists) {
            return "The song was previously on the playlist";
        }

        // getSongById đã filter banned songs rồi
        Song song = songService.getSongById(songId);
        if (song == null) {
            return "Song not found or has been banned";
        }

        PlaylistItem newItem = new PlaylistItem();
        newItem.setSong(song);
        newItem.setPlaylist(playlist);
        playlist.getPlayListItem().add(newItem);
        playListRepository.save(playlist);

        return "Song added to playlist successfully";
    }

    public String removeSongFromPlaylist(String authorizationHeader, Long playlistId, Long songId) {
        Playlist playlist = playListRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        if (!playlist.getAuth().getId().equals(user.getId())) {
            return "You do not have permission to modify this playlist";
        }

        boolean removed = playlist.getPlayListItem().removeIf(item ->
                item.getSong().getId().equals(songId)
        );

        if (removed) {
            playListRepository.save(playlist); // orphanRemoval = true sẽ xoá luôn PlaylistItem trong DB
            return "Song removed from playlist successfully";
        } else {
            return "Song not found in playlist";
        }
    }

    public List<PlaylistResponse> getFollowedPlaylists(String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        List<Playlist> playlists = playListRepository.findAllByAuthId(user.getId());

        // Filter playlists: chỉ hiển thị playlists có ít nhất 1 bài hát ACTIVE
        return playlists.stream()
                .filter(playlist -> {
                    boolean hasActiveSongs = playlist.getPlayListItem().stream()
                            .anyMatch(item -> item.getSong() != null
                                    && item.getSong().getStatus() == SongStatusEnum.ACTIVE);
                    return hasActiveSongs;
                })
                .map(playlistMapper::toResponse)
                .toList();
    }

    public String deletePlaylist(String authorizationHeader, Long playlistId) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        Playlist playlist = playListRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        if (!playlist.getAuth().getId().equals(user.getId())) {
            return "You do not have permission to delete this playlist";
        }

        playListRepository.delete(playlist);

        return "Playlist deleted successfully";
    }

    public PlaylistResponse createPlayListWithFirstSong(String authorizationHeader, Long songId) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        if (user == null) {
            throw new RuntimeException("Invalid token");
        }

        // getSongById đã filter banned songs rồi
        Song song = songService.getSongById(songId);
        if (song == null) {
            throw new RuntimeException("Song not found or has been banned");
        }

        Playlist playlist = new Playlist();
        playlist.setName(song.getTitle());
        playlist.setImageUrl(song.getImageUrl());
        playlist.setAuth(user);

        PlaylistItem item = new PlaylistItem();
        item.setPlaylist(playlist);
        item.setSong(song);
        playlist.getPlayListItem().add(item);

        playListRepository.save(playlist);
        return playlistMapper.toResponse(playlist);
    }

    public PlaylistWithListSongResponse getPlaylistWithListSongResponseById(String authorizationHeader, Long playlistId) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        Playlist playlist = playListRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        if (!playlist.getAuth().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to access this playlist");
        }

        PlaylistResponse playlistResponse = playlistMapper.toResponse(playlist);

        // Filter banned songs khỏi playlist
        List<SongAndArtistResponse> songAndArtistResponses = playlist.getPlayListItem().stream()
                .map(PlaylistItem::getSong)
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED) // Filter banned songs
                .map(song -> {
                    SongAndArtistResponse songAndArtistResponse = new SongAndArtistResponse();
                    SongResponse songResponse = songMapper.toSongResponse(song);
                    songAndArtistResponse.setSong(songResponse);
                    Auth artist = song.getAuth();
                    ArtistResponse artistResponse = userMapper.toArtistResponse(artist);
                    Long monthlyListeners = listeningHistoryService.calculateAverageMonthlyListeners(artist.getId());
                    artistResponse.setMonthlyListeners(monthlyListeners);
                    songAndArtistResponse.setArtist(artistResponse);
                    return songAndArtistResponse;
                })
                .collect(Collectors.toList());

        return new PlaylistWithListSongResponse(playlistResponse, songAndArtistResponses);
    }
}
