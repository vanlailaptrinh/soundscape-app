package com.example.soundscape_app.entity.album;

import com.example.soundscape_app.entity.auth.Auth;
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


@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String coverUrl;
    private String description;

    @ManyToOne
    @JoinColumn(name = "auth_id", nullable = false)
    private Auth auth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumItem> albumItems = new ArrayList<>();

    @ManyToMany(mappedBy = "followingAlbums")
    private Set<Auth> followers = new HashSet<>();
}
