package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AppListeningStatsResponse {
    private List<AppListeningChartPoint> chart;
    private List<TopSongStat> topSongs;
}
