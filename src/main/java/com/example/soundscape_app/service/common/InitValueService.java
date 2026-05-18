package com.example.soundscape_app.service.common;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.playlist.Playlist;
import com.example.soundscape_app.repository.playlist.PlayListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitValueService {
    private final PlayListRepository playListRepository;
    private final String DEFAULT_PLAYLIST_IMAGE_URL = "https://spotifybe.s3.ap-southeast-1.amazonaws.com/asset/defaultvalue/playlistdefault.jpg";

    public void initPlayListForUser(Auth auth) {
        Playlist playlist = new Playlist();
        playlist.setName("Liked Songs");
        playlist.setAuth(auth);
        playlist.setImageUrl(DEFAULT_PLAYLIST_IMAGE_URL);
        playListRepository.save(playlist);
    }
}