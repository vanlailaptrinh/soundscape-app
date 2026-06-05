package com.example.soundscape_app.controller.song;

import com.example.soundscape_app.dto.request.song.SongRatingRequest;
import com.example.soundscape_app.dto.response.song.SongRatingStatsResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.service.song.SongRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/song/ratings")
@RequiredArgsConstructor
public class SongRatingController {

    private final SongRatingService songRatingService;

    //-------------------- User ---------------------//
    @PostMapping("/{songId}/rate")
    public SongResponse rateSong(
            @RequestBody SongRatingRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        return songRatingService.rateSong(request, authorizationHeader);
    }

    @DeleteMapping("/{songId}/rate")
    public SongResponse unrateSong(
            @PathVariable Long songId,
            @RequestHeader("Authorization") String authorizationHeader) {
        return songRatingService.unrateSong(songId, authorizationHeader);
    }

    @GetMapping("/{songId}/rate")
    public Integer getUserRating(
            @PathVariable Long songId,
            @RequestHeader("Authorization") String authorizationHeader) {
        return songRatingService.getUserRating(songId, authorizationHeader);
    }

    @GetMapping("/{songId}/rating-average")
    public Double getAverageRating(@PathVariable Long songId) {
        return songRatingService.getAverageRating(songId);
    }

    @GetMapping("/{songId}/rating-stats")
    public SongRatingStatsResponse getRatingStats(@PathVariable Long songId) {
        return songRatingService.getRatingStats(songId);
    }

}

