package com.example.soundscape_app.dto.response.album;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlbumTrendingResponse {
    private Long id;
    private String coverUrl;
    private String name;
    private Long artistId;
    private String username;
}
