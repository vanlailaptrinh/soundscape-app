package com.example.soundscape_app.dto.ai;

import com.example.soundscape_app.dto.response.song.PlaylistWithListSongResponse;

public record SmartPlaylistResponse(
        SmartPlaylistAiResult ai,
        PlaylistWithListSongResponse playlist) {
}
