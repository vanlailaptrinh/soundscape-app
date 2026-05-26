package com.example.soundscape_app.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyListeningTime {
    private String day;
    private Long totalDuration;
}
