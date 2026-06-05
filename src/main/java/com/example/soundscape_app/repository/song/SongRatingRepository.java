package com.example.soundscape_app.repository.song;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.entity.song.SongRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRatingRepository extends JpaRepository<SongRating, Long> {

    Optional<SongRating> findBySongAndUser(Song song, Auth user);

    List<SongRating> findAllBySong(Song song);

    long countBySong(Song song);
}
