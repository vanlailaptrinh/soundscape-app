package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopSongStat {
    private Long songId;
    private String title;
    private String artist;
    private Long listeningCount;
}
