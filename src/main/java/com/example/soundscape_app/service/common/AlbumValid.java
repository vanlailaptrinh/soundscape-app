package com.example.soundscape_app.service.common;

import com.example.soundscape_app.entity.album.Album;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.exception.UnauthorizedAccessException;
import com.example.soundscape_app.repository.album.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumValid {
    private final AlbumRepository albumRepository;

    public Album validateAndGetAlbumIfNeeded(Long albumId, Auth auth) {
        if (albumId == null) return null;

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new UnauthorizedAccessException("Album không tồn tại"));
        if (!album.getAuth().getId().equals(auth.getId())) {
            throw new UnauthorizedAccessException("Album không thuộc về người dùng.");
        }

        return album;
    }
}
