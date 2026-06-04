package com.example.soundscape_app.controller.song;

import com.example.soundscape_app.dto.request.song.SongRequest;
import com.example.soundscape_app.dto.response.song.DailyListeningTime;
import com.example.soundscape_app.dto.response.song.ListeningHistoryResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.dto.response.song.SongTrendingResponse;
import com.example.soundscape_app.dto.response.song.SongWithArtistResponse;
import com.example.soundscape_app.service.song.SongService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SongController {

    private final SongService songService;

    //-------------------- Open ---------------------//
    @GetMapping("/open/songs/trending")
    public Page<SongTrendingResponse> getTrendingSongs(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "7") double tau) {
        return songService.getTrendingSongs(pageable, tau);
    }

    @GetMapping("/open/songs/recent")
    public Page<SongTrendingResponse> getRecentSongs(
            @PageableDefault(size = 10) Pageable pageable) {
        return songService.getRecentSongs(pageable);
    }

    @GetMapping("/open/songs/{songId}")
    public ResponseEntity<SongWithArtistResponse> getSongWithArtist(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long songId) {
        return ResponseEntity.ok(songService.getSongWithArtist(authorizationHeader, songId));
    }


    //-------------------- User ---------------------//
    @GetMapping("/user/songs/{songId}/listen")
    @Transactional
    public ResponseEntity<String> listenSong(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long songId) {
        songService.listenSong(authorizationHeader, songId);
        return ResponseEntity.ok("Listening event recorded.");
    }

    @GetMapping("/user/listening-history")
    public ResponseEntity<Page<ListeningHistoryResponse>> getUniqueListeningHistory(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PageableDefault(page = 0, size = 8) Pageable pageable) {

        Page<ListeningHistoryResponse> history = songService.getUniqueListeningHistory(authorizationHeader, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/daily-time")
    public ResponseEntity<List<DailyListeningTime>> getUserDailyListeningTime(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestParam(name = "days", defaultValue = "30") int days) {
        List<DailyListeningTime> dailyTime = songService.getUserDailyListeningTime(authorizationHeader, days);
        return ResponseEntity.ok(dailyTime);
    }

    @PutMapping("user/songs/{songId}/cal-duration-song")
    @Transactional
    public void calDurationSong(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long songId) {
        songService.calDurationSong(authorizationHeader, songId);
    }

    @GetMapping("/user/recommend/songs")
    public ResponseEntity<Page<SongTrendingResponse>> getRecommendedSongs(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SongTrendingResponse> recommendedSongs = songService.getListSongsRecommend(authorizationHeader, pageable);
        return ResponseEntity.ok(recommendedSongs);
    }

    //-------------------- Artist ---------------------//
    @PostMapping("/artist/upload-song")
    public ResponseEntity<SongResponse> uploadSong(HttpServletRequest request, @ModelAttribute SongRequest songRequest) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        SongResponse songResponse = songService.uploadSong(songRequest, authorizationHeader);
        return ResponseEntity.ok(songResponse);
    }

    @GetMapping("/artist/get-songs")
    public ResponseEntity<List<SongResponse>> getMySongs(
            @RequestHeader("Authorization") String authorizationHeader) {
        List<SongResponse> mySongs = songService.getMySongs(authorizationHeader);
        return ResponseEntity.ok(mySongs);
    }

}
