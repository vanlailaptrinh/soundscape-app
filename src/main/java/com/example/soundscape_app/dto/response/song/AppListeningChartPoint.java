package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppListeningChartPoint {
    private String date;   // yyyy-MM-dd
    private Long count;
}
