package com.example.soundscape_app.controller.album;

import com.example.soundscape_app.dto.request.song.AlbumRequest;
import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.dto.response.album.AlbumTrendingResponse;
import com.example.soundscape_app.service.album.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AlbumController {

    private final AlbumService albumService;

    //-------------------- Open ---------------------//
    @GetMapping("/open/albums/trending")
    public Page<AlbumTrendingResponse> getTopAlbums(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return albumService.getAlbumsTrending(pageable);
    }

    //-------------------- User ---------------------//

    //-------------------- Artist ---------------------//
    @PostMapping("/artist/create-album")
    public ResponseEntity<AlbumResponse> createAlbum(
            @ModelAttribute AlbumRequest albumRequest,
            @RequestHeader("Authorization") String authorizationHeader) {
        AlbumResponse response = albumService.createAlbum(albumRequest, authorizationHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/artist/{albumId}/add-song/{songId}")
    public ResponseEntity<String> addSongToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId,
            @RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok(albumService.addSongToAlbum(albumId, songId, authorizationHeader));
    }

    @GetMapping("/artist/get-my-albums")
    public ResponseEntity<List<AlbumResponse>> getMyAlbums(
            @RequestHeader("Authorization") String authorizationHeader) {
        List<AlbumResponse> albums = albumService.getMyAlbums(authorizationHeader);
        return ResponseEntity.ok(albums);
    }

    @DeleteMapping("/artist/delete-album/{albumId}")
    public ResponseEntity<String> deleteAlbum(
            @PathVariable Long albumId,
            @RequestHeader("Authorization") String authorizationHeader) {
        String message = albumService.deleteAlbum(albumId, authorizationHeader);
        return ResponseEntity.ok(message);
    }

}

