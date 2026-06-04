package com.example.soundscape_app.service.user;

import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.dto.response.album.AlbumWithSongsResponse;
import com.example.soundscape_app.dto.response.song.SongAndArtistResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.dto.response.user.ArtistWithSongsAndAlbumsResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.mapper.song.AlbumMapper;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.repository.artist.ArtistRepository;
import com.example.soundscape_app.service.album.AlbumService;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.song.ListeningHistoryService;
import com.example.soundscape_app.service.song.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final UserMapper userMapper;
    private final ArtistRepository artistRepository;
    private final SongService songService;
    private final AuthService authService;
    private final AlbumMapper albumMapper;
    private final AlbumService albumService;
    private final ListeningHistoryService listeningHistoryService;

    public ArtistResponse getArtistBySongId(Long songId) {
        Auth auth = artistRepository.findBySongsId(songId)
                .orElseThrow(() -> new RuntimeException("Artist not found for song id: " + songId));
        return userMapper.toArtistResponse(auth);
    }

    public ArtistWithSongsAndAlbumsResponse getArtistWithSongsAndAlbums(Long artistId) {
        Auth artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found for id: " + artistId));

        // getSongsByArtistId đã filter banned songs rồi
        List<SongResponse> songs = songService.getSongsByArtistId(artistId);

        // Filter albums: chỉ lấy albums có ít nhất 1 bài hát ACTIVE
        List<AlbumResponse> albums = artist.getAlbums().stream()
                .filter(album -> {
                    boolean hasActiveSongs = album.getAlbumItems().stream()
                            .anyMatch(item -> item.getSong() != null
                                    && item.getSong().getStatus() == SongStatusEnum.ACTIVE);
                    return hasActiveSongs;
                })
                .map(albumMapper::toResponse)
                .collect(Collectors.toList());

        Long averageMonthlyListeners = listeningHistoryService.calculateAverageMonthlyListeners(artist.getId());
        ArtistResponse artistResponse = userMapper.toArtistResponse(artist);
        artistResponse.setMonthlyListeners(averageMonthlyListeners);

        return new ArtistWithSongsAndAlbumsResponse(
                artistResponse,
                songs,
                albums
        );
    }

    public Page<ArtistResponse> getTrendingArtists(Pageable pageable, double tau) {
        return artistRepository.findTrendingArtists(pageable, tau);
    }

    public SongAndArtistResponse getSongAndArtistById(Long songId) {
        // getSongResponseById đã filter banned songs rồi
        SongResponse songResponse = songService.getSongResponseById(songId);

        // Nếu bài hát bị banned, songResponse sẽ null
        if (songResponse == null) {
            throw new RuntimeException("Song not found or has been banned with id: " + songId);
        }

        ArtistResponse artistResponse = getArtistBySongId(songId);
        if (artistResponse == null) {
            throw new RuntimeException("Artist not found for song id: " + songId);
        }

        Long averageMonthlyListeners = listeningHistoryService.calculateAverageMonthlyListeners(artistResponse.getId());
        artistResponse.setMonthlyListeners(averageMonthlyListeners);

        return new SongAndArtistResponse(songResponse, artistResponse);
    }

    public Auth getArtistById(Long artistId) {
        return artistRepository.findById(artistId)
                .orElse(null);
    }

    public List<ArtistResponse> getFollowedArtists(String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        if (user == null) throw new RuntimeException("Invalid token");

        return user.getFollowingArtists().stream()
                .map(userMapper::toArtistResponse)
                .toList();
    }

    public AlbumWithSongsResponse getAlbumWithSongs(Long albumId) {
        Album album = albumService.getAlbumById(albumId);
        Auth artist = album.getAuth();

        // getListSongResponseById đã filter banned songs rồi
        List<SongResponse> songs = albumService.getListSongResponseById(albumId);

        Long averageMonthlyListeners = listeningHistoryService.calculateAverageMonthlyListeners(artist.getId());
        ArtistResponse artistResponse = userMapper.toArtistResponse(artist);
        artistResponse.setMonthlyListeners(averageMonthlyListeners);

        AlbumResponse albumResponse = albumMapper.toResponse(album);
        return new AlbumWithSongsResponse(albumResponse, songs, artistResponse);
    }

    public List<ArtistResponse> getAllArtists() {
        return artistRepository.findAllArtists().stream()
                .map(userMapper::toArtistResponse)
                .toList();
    }
}
