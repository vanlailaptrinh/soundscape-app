package com.example.soundscape_app.controller.artist;

import com.example.soundscape_app.dto.response.album.AlbumWithSongsResponse;
import com.example.soundscape_app.dto.response.song.SongAndArtistResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.dto.response.user.ArtistWithSongsAndAlbumsResponse;
import com.example.soundscape_app.service.user.ArtistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/")
public class ArtistController {

    private final ArtistService artistService;

    //-------------------- Open ---------------------/

    @GetMapping("open/artist/trending")
    public Page<ArtistResponse> getTrendingArtists(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(defaultValue = "7") double tau) {
        return artistService.getTrendingArtists(pageable, tau);
    }

    @GetMapping("open/{artistId}/full")
    public ArtistWithSongsAndAlbumsResponse getArtistWithSongsAndAlbums(@PathVariable Long artistId) {
        return artistService.getArtistWithSongsAndAlbums(artistId);
    }

    @GetMapping("open/songs/{songId}")
    @Transactional
    public ResponseEntity<SongAndArtistResponse> getSongAndArtistById(@PathVariable Long songId) {
        SongAndArtistResponse response = artistService.getSongAndArtistById(songId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("open/albums/{albumId}/songs")
    public ResponseEntity<AlbumWithSongsResponse> getAlbumWithSongs(@PathVariable Long albumId) {
        AlbumWithSongsResponse response = artistService.getAlbumWithSongs(albumId);
        return ResponseEntity.ok(response);
    }
    //-------------------- User ---------------------//
}

