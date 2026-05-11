package com.example.soundscape_app.entity.song;

import com.example.soundscape_app.entity.auth.Auth;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "song_ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"song_id", "user_id"})
)
public class SongRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Auth user;

    @Column(nullable = false)
    private int rating; // 1–5
}

