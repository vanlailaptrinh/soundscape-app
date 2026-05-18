package com.example.soundscape_app.repository.song;

import com.example.soundscape_app.entity.song.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongGenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findAll();
}
