package com.example.soundscape_app.service.song;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.request.song.FollowRequest;
import com.example.soundscape_app.dto.request.song.UnFollowRequest;
import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.dto.response.song.PlaylistResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.dto.response.user.FollowedResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.exception.InvalidTokenException;
import com.example.soundscape_app.repository.user.UserRepository;
import com.example.soundscape_app.service.album.AlbumService;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.playlist.PlaylistService;
import com.example.soundscape_app.service.user.ArtistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final AuthService authService;
    private final ArtistService artService;
    private final AlbumService albumService;
    private final UserRepository userRepository;
    private final PlaylistService playlistService;
    private final ArtistService artistService;

    public String follow(String authorizationHeader, FollowRequest followRequest) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        if (user == null) {
            throw new InvalidTokenException("Invalid token");
        }

        String message;
        Long id = followRequest.getId();
        switch (followRequest.getType()) {
            case ARTIST -> {
                var artist = artService.getArtistById(id);
                if (artist == null) throw new IllegalArgumentException("Artist không tồn tại");

                boolean alreadyFollowed = user.getFollowingArtists()
                        .stream()
                        .anyMatch(a -> a.getId().equals(artist.getId()));

                if (alreadyFollowed)
                    return ("Đã follow nghệ sĩ này rồi");

                user.getFollowingArtists().add(artist);
                message = "Follow nghệ sĩ thành công";
            }
            case ALBUM -> {
                var album = albumService.getAlbumById(id);
                if (album == null) throw new IllegalArgumentException("Album không tồn tại");

                boolean alreadyFollowed = user.getFollowingAlbums()
                        .stream()
                        .anyMatch(a -> a.getId().equals(album.getId()));

                if (alreadyFollowed)
                    throw new IllegalStateException("Đã follow album này rồi");

                user.getFollowingAlbums().add(album);
                message = "Follow album thành công";
            }
            default -> throw new IllegalArgumentException("Loại follow không hợp lệ");
        }

        userRepository.save(user);
        return message;
    }

    public String unfollow(String authorizationHeader, UnFollowRequest unFollowRequest) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        if (user == null) {
            throw new InvalidTokenException("Invalid token");
        }

        String message;
        Long id = unFollowRequest.getId();
        switch (unFollowRequest.getType()) {
            case ARTIST -> {
                var artist = artService.getArtistById(id);
                if (artist == null || !user.getFollowingArtists().remove(artist))
                    throw new IllegalStateException("Chưa follow nghệ sĩ này");
                message = "Unfollow nghệ sĩ thành công";
            }
            case ALBUM -> {
                var album = albumService.getAlbumById(id);
                if (album == null || !user.getFollowingAlbums().remove(album))
                    throw new IllegalStateException("Chưa follow album này");
                message = "Unfollow album thành công";
            }
            default -> throw new IllegalArgumentException("Loại follow không hợp lệ");
        }
        userRepository.save(user);
        return message;
    }

    public FollowedResponse followed(String authorizationHeader) {
        List<PlaylistResponse> playlistFollowed = playlistService.getFollowedPlaylists(authorizationHeader);
        List<ArtistResponse> artistFollowed = artistService.getFollowedArtists(authorizationHeader);
        List<AlbumResponse> albumFollowed = albumService.getFollowedAlbums(authorizationHeader);

        return new FollowedResponse(playlistFollowed, artistFollowed, albumFollowed);
    }

    public List<ArtistResponse> followedArtist(String authorizationHeader) {
        return artistService.getFollowedArtists(authorizationHeader);
    }

    public List<AlbumResponse> followedAlbum(String authorizationHeader) {
        return albumService.getFollowedAlbums(authorizationHeader);
    }
}
