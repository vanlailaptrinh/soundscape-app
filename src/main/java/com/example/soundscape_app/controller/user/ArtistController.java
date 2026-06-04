package com.example.soundscape_app.controller.user;

import com.example.soundscape_app.dto.response.user.ArtistWithSongsAndAlbumsResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.service.user.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/open/{artistId}/full")
    public ResponseEntity<ArtistWithSongsAndAlbumsResponse> getArtistWithSongsAndAlbums(
            @PathVariable Long artistId) {
        return ResponseEntity.ok(artistService.getArtistWithSongsAndAlbums(artistId));
    }

    @GetMapping("/open/artists")
    public ResponseEntity<List<ArtistResponse>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }
}
