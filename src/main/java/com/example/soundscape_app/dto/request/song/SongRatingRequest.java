package com.example.soundscape_app.dto.request.song;

import lombok.Data;

@Data
public class SongRatingRequest {
    private Long songId;
    private int rating;
}
