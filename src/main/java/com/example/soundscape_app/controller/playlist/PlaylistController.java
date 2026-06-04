package com.example.soundscape_app.controller.playlist;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.soundscape_app.dto.request.song.CreatePlaylistRequest;
import com.example.soundscape_app.dto.response.song.PlaylistResponse;
import com.example.soundscape_app.dto.response.song.PlaylistWithListSongResponse;
import com.example.soundscape_app.service.playlist.PlaylistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PlaylistController {

    private final PlaylistService playlistService;

    //-------------------- User ---------------------//
    @PostMapping("user/{playlistId}/songs/{songId}")
    public ResponseEntity<String> addSongToPlaylist(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        return ResponseEntity.ok(playlistService.addSongToPlaylist(authorizationHeader, playlistId, songId));
    }

    // Xoá bài hát khỏi playlist
    @DeleteMapping("user/{playlistId}/songs/{songId}")
    public ResponseEntity<String> removeSongFromPlaylist(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        return ResponseEntity.ok(playlistService.removeSongFromPlaylist(authorizationHeader, playlistId, songId));
    }

    @GetMapping("user/my-playlists")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(
            @RequestHeader("Authorization") String authorizationHeader) {
        List<PlaylistResponse> playlists = playlistService.getFollowedPlaylists(authorizationHeader);
        return ResponseEntity.ok(playlists);
    }

    @PostMapping("user/create-playlist-with-song")
    public ResponseEntity<PlaylistResponse> createPlaylistWithFirstSong(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreatePlaylistRequest request) {
        PlaylistResponse response = playlistService.createPlayListWithFirstSong(authorizationHeader, request.getSongId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("user/playlist/{playlistId}")
    public ResponseEntity<PlaylistWithListSongResponse> getPlaylistWithSongs(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long playlistId) {
        PlaylistWithListSongResponse response =
                playlistService.getPlaylistWithListSongResponseById(authorizationHeader, playlistId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("user/{playlistId}")
    public ResponseEntity<String> deletePlaylist(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long playlistId) {
        String result = playlistService.deletePlaylist(authorizationHeader, playlistId);
        return ResponseEntity.ok(result);
    }


}
