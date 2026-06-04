package com.example.soundscape_app.service.user;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.response.album.AlbumTrendingResponse;
import com.example.soundscape_app.dto.response.song.SongTrendingResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.repository.album.AlbumRepository;
import com.example.soundscape_app.repository.artist.ArtistRepository;
import com.example.soundscape_app.repository.song.SongRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    public Map<String, Object> searchAll(String keyword) {
        List<SongTrendingResponse> songs = songRepository.findByNormalizedSearch(keyword);
        List<AlbumTrendingResponse> albums = albumRepository.findByNormalizedSearch(keyword);
        List<ArtistResponse> users = artistRepository.findByNormalizedSearch(keyword);

        return Map.of(
                "songs", songs,
                "artists", users,
                "albums", albums
        );
    }

}
