package com.spotify.entity.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spotify.entity.album.Album;
import com.spotify.entity.song.ListeningHistory;
import com.spotify.entity.song.Song;
import com.spotify.enums.AccountStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auths")
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    // For traditional login only
    private String password;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(unique = true)
    private String urlAvatar;

    @JsonIgnore
    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "auth_roles",
            joinColumns = @JoinColumn(name = "auth_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roleEntities = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProvider> authProviders = new ArrayList<>();

    //--------------User--------------
    @JsonIgnore
    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListeningHistory> listeningHistories = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_follow_artists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Auth> followingArtists = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_follow_albums",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    private Set<Album> followingAlbums = new HashSet<>();

    //--------------Artist--------------
    @JsonIgnore
    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Song> songs = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albums = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'ACTIVE'")
    private AccountStatusEnum status = AccountStatusEnum.ACTIVE;
}
