package com.example.soundscape_app.service.album;

import com.example.soundscape_app.dto.request.song.AlbumRequest;
import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.dto.response.album.AlbumTrendingResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.album.AlbumItem;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.enums.SongStatusEnum;
import com.example.soundscape_app.mapper.song.AlbumMapper;
import com.example.soundscape_app.mapper.song.SongMapper;
import com.example.soundscape_app.repository.album.AlbumRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.common.AlbumValid;
import com.example.soundscape_app.service.song.SongService;
import com.example.soundscape_app.util.S3Util;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AuthService authService;
    private final AlbumMapper albumMapper;
    private final SongService songService;
    private final S3Util s3Util;
    private final AlbumValid albumValid;
    private final SongMapper songMapper;


    public AlbumResponse createAlbum(AlbumRequest albumRequest, String authorizationHeader) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Album album = Album.builder()
                .name(albumRequest.getName())
                .coverUrl(s3Util.uploadImageIfPresent(albumRequest.getFileCover()))
                .auth(auth)
                .albumItems(new ArrayList<>())
                .description(albumRequest.getDescription())
                .build();

        album = albumRepository.save(album);
        return albumMapper.toResponse(album);
    }

    public String addSongToAlbum(Long albumId, Long songId, String authorizationHeader) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with id: " + albumId));

        // getSongById đã filter banned songs rồi
        Song song = songService.getSongById(songId);
        if (song == null) {
            throw new RuntimeException("Song not found or has been banned with id: " + songId);
        }

        boolean exists = album.getAlbumItems().stream()
                .anyMatch(item -> item.getSong().getId().equals(songId));
        if (exists) {
            return "Song already exists in this album " + albumId;
        }

        albumValid.validateAndGetAlbumIfNeeded(album.getId(), auth);

        AlbumItem albumItem = new AlbumItem();
        albumItem.setAlbum(album);
        albumItem.setSong(song);
        album.getAlbumItems().add(albumItem);
        albumRepository.save(album);

        return "Added to album " + albumId;
    }

    public Page<AlbumTrendingResponse> getAlbumsTrending(Pageable pageable) {
        Page<AlbumTrendingResponse> allAlbums = albumRepository.getAlbumTrending(pageable);

        // Filter albums: chỉ hiển thị albums có ít nhất 1 bài hát ACTIVE
        List<AlbumTrendingResponse> activeAlbums = allAlbums.getContent().stream()
                .filter(albumResponse -> {
                    Album album = albumRepository.findById(albumResponse.getId()).orElse(null);
                    if (album == null) return false;

                    // Kiểm tra xem album có bài hát ACTIVE không
                    boolean hasActiveSongs = album.getAlbumItems().stream()
                            .anyMatch(item -> item.getSong() != null
                                    && item.getSong().getStatus() == SongStatusEnum.ACTIVE);
                    return hasActiveSongs;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(activeAlbums, pageable, activeAlbums.size());
    }

    public Album getAlbumById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));
    }

    public List<AlbumResponse> getFollowedAlbums(String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        if (user == null) throw new RuntimeException("Invalid token");

        // Filter albums: chỉ hiển thị albums có ít nhất 1 bài hát ACTIVE
        return user.getFollowingAlbums().stream()
                .filter(album -> {
                    boolean hasActiveSongs = album.getAlbumItems().stream()
                            .anyMatch(item -> item.getSong() != null
                                    && item.getSong().getStatus() == SongStatusEnum.ACTIVE);
                    return hasActiveSongs;
                })
                .map(albumMapper::toResponse)
                .toList();
    }

    public List<SongResponse> getListSongResponseById(Long id) {
        List<Song> songs = albumRepository.findAllSongsByAlbumId(id);

        // Filter banned songs
        return songs.stream()
                .filter(song -> song.getStatus() != SongStatusEnum.BANNED)
                .map(songMapper::toSongResponse)
                .toList();
    }

    public List<AlbumResponse> getMyAlbums(String authorizationHeader) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        List<Album> albums = albumRepository.findByAuthId(auth.getId());

        // Filter albums: chỉ hiển thị albums có ít nhất 1 bài hát ACTIVE
        return albums.stream()
                .filter(album -> {
                    boolean hasActiveSongs = album.getAlbumItems().stream()
                            .anyMatch(item -> item.getSong() != null
                                    && item.getSong().getStatus() == SongStatusEnum.ACTIVE);
                    return hasActiveSongs;
                })
                .map(albumMapper::toResponse)
                .collect(Collectors.toList());
    }

    public String deleteAlbum(Long albumId, String authorizationHeader) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with id: " + albumId));

        albumValid.validateAndGetAlbumIfNeeded(album.getId(), auth);
        String albumName = album.getName();

        if (album.getFollowers() != null) {
            album.getFollowers().forEach(user -> user.getFollowingAlbums().remove(album));
            album.getFollowers().clear();
        }

        albumRepository.delete(album);

        return "Deleted album " + albumName + " successfully";
    }
}
