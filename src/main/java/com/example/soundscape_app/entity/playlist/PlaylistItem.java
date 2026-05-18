package com.example.soundscape_app.entity.playlist;

import com.example.soundscape_app.entity.song.Song;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "playlist_items")
public class PlaylistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "last_listened_at", nullable = false)
    private LocalDateTime lastListenedAt = LocalDateTime.now();
}