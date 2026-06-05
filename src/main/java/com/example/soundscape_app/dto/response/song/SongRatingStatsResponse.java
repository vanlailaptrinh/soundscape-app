package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongRatingStatsResponse {
    private Double average;
    private Long total;
    private Map<Integer, Long> distribution;
}
