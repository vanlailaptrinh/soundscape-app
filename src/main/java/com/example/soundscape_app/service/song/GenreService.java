package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.response.song.GenreResponse;
import com.example.soundscape_app.entity.song.Genre;
import com.example.soundscape_app.mapper.song.GenreMapper;
import com.example.soundscape_app.repository.song.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final SongGenreRepository songGenreRepository;
    private final GenreMapper genreMapper;

    public Set<Genre> getGenresFromIds(List<Long> genreIds) {
        return genreIds.stream()
                .map(id -> songGenreRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Thể loại không tồn tại với ID: " + id)))
                .collect(Collectors.toSet());
    }

    public List<GenreResponse> getAllGenres() {
        return genreMapper.toResponseList(songGenreRepository.findAll());
    }
}

