package com.example.soundscape_app.controller.song;

import com.example.soundscape_app.dto.response.song.AppListeningStatsResponse;
import com.example.soundscape_app.dto.response.song.DailyListeningStat;
import com.example.soundscape_app.dto.response.song.DailyListeningTime;
import com.example.soundscape_app.service.song.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    //---------------------Admin---------------------------

    @GetMapping("/admin/statistics/song/{songId}/listening")
    public ResponseEntity<List<DailyListeningStat>> getSongMonthlyStats(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "6") int months) {
        List<DailyListeningStat> stats = statisticService.getSongStats(songId, months);
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/artist/song/{songId}/listening/daily")
    public ResponseEntity<List<DailyListeningStat>> getSongDailyStats(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long songId,
            @RequestParam(defaultValue = "30") int days) {
        if (days <= 0) days = 30;

        List<DailyListeningStat> stats = statisticService.getSongStats(songId, days, authorizationHeader);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/statistics/analyse")
    public AppListeningStatsResponse getApplicationStats(@RequestParam(defaultValue = "30") int days) {
        if (days <= 0) days = 30;
        return statisticService.getStats(days);
    }

    @GetMapping("user/daily-time")
    public List<DailyListeningTime> getUserDailyListeningTime(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam int days) {
        return statisticService.getUserDailyListeningTime(authorizationHeader, days);
    }
}

