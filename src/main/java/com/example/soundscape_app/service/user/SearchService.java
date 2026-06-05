package com.example.soundscape_app.service.user;

import java.util.List;
import java.util.Map;

import com.example.soundscape_app.dto.response.user.ArtistProjection;
import com.example.soundscape_app.util.S3Util;
import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.response.album.AlbumTrendingCdnResponse;
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
    private final S3Util s3Util;

    public Map<String, Object> searchAll(String keyword) {
        List<SongTrendingResponse> songs = songRepository.findByNormalizedSearch(keyword);
        List<AlbumTrendingResponse> albums = albumRepository.findByNormalizedSearch(keyword).stream()
                .<AlbumTrendingResponse>map(album -> new AlbumTrendingCdnResponse(
                        album.getId(),
                        s3Util.toCdnUrl(album.getCoverUrl()),
                        album.getName(),
                        album.getArtistId(),
                        album.getUsername()
                ))
                .toList();
        List<ArtistProjection> projections = artistRepository.findByNormalizedSearch(keyword);
        List<ArtistResponse> users = projections.stream()
                .map(p -> new ArtistResponse(p.getId(), p.getUsername(), p.getUrlAvatar(), p.getMonthlyListeners()))
                .toList();

        return Map.of(
                "songs", songs,
                "artists", users,
                "albums", albums
        );
    }

}
