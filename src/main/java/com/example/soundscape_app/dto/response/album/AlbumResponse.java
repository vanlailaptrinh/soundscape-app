package com.example.soundscape_app.dto.response.album;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class AlbumResponse {
    private Long id;
    private String name;
    private String coverUrl;
    private String description;
    private LocalDateTime createdAt;
}
