package com.example.soundscape_app.dto.response.song;

public interface SongTrendingResponse {
    Long getId();
    String getTitle();
    String getImageUrl();
    String getAuthor();
    Long getArtistId();
    String getUsername();
}