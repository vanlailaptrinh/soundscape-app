package com.example.soundscape_app.dto.response.album;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AlbumTrendingCdnResponse implements AlbumTrendingResponse {
    private final Long id;
    private final String coverUrl;
    private final String name;
    private final Long artistId;
    private final String username;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getCoverUrl() {
        return coverUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getArtistId() {
        return artistId;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
