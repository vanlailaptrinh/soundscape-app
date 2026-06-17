package com.example.soundscape_app.dto.ai;

public record SmartPlaylistAiResult(
        String playlistName,
        String keyword,
        String genre,
        Integer durationMinutes) {
}
