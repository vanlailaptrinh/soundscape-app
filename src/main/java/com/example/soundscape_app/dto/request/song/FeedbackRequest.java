package com.example.soundscape_app.dto.request.song;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Long songId;
    private Integer rating;
    private String source;
}