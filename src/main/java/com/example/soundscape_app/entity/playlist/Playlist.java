package com.example.soundscape_app.entity.playlist;

import com.example.soundscape_app.entity.auth.Auth;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "playlists")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "auth_id", nullable = false)
    private Auth auth;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistItem> playListItem = new ArrayList<>();
}