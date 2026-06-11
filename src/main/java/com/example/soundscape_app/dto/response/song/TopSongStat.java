package com.example.soundscape_app.dto.response.song;

public interface TopSongStat {
    Long getSongId();

    String getTitle();

    String getArtist();

    Long getListeningCount();
}
