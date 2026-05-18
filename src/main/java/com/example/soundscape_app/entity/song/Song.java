package com.example.soundscape_app.entity.song;

import com.example.soundscape_app.entity.album.AlbumItem;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.playlist.PlaylistItem;
import com.example.soundscape_app.enums.MediaEnum;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String mediaUrl;

    private String imageUrl;

    private String author;

    @Column(nullable = false)
    private int playCount = 0;

    private String description;

    @ManyToOne
    @JoinColumn(name = "auth_id", nullable = false)
    private Auth auth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "song_genres",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MediaEnum type;

    @JsonIgnore
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistItem> playlistItemEntities = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumItem> albumItems = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListeningHistory> listeningHistories = new ArrayList<>();

    private Double duration;
    private Double tempo;
    private Double energy;
    private Double loudness;
    private Double danceability;
    private Double acousticness;
    private Double instrumentalness;
    private Double liveness;
    private Double speechiness;
    private Double valence;

    @Column(name = "rating_avg", nullable = false)
    @Builder.Default
    private Double ratingAvg = 0.0;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'ACTIVE'")
    private SongStatusEnum status = SongStatusEnum.ACTIVE;

}

