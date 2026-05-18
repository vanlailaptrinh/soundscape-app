package com.example.soundscape_app.repository.playlist;

import com.example.soundscape_app.entity.playlist.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayListRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findAllByAuthId(Long authId);

}
