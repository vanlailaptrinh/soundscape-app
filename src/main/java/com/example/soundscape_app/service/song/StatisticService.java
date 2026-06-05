package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.response.song.*;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.repository.song.ListeningHistoryRepository;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final ListeningHistoryRepository listeningHistoryRepository;
    private final SongRepository songRepository;
    private final AuthService authService;

    public List<DailyListeningStat> getSongStats(Long songId, int days) {
        return listeningHistoryRepository.getSongDailyStats(songId, days);
    }

    public List<DailyListeningStat> getSongStats(Long songId, int days, String authorizationHeader) {

        Auth currentUser = authService.getAuthFromAccessToken(authorizationHeader);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (!song.getAuth().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this song");
        }

        return listeningHistoryRepository.getSongDailyStats(songId, days);
    }


    public AppListeningStatsResponse getStats(int days) {

        List<AppListeningChartPoint> chart = listeningHistoryRepository.getAppDailyListeningStats(days);
        List<TopSongStat> topSongs = listeningHistoryRepository.getTopSongsApp(days);

        AppListeningStatsResponse response = new AppListeningStatsResponse();
        response.setChart(chart);
        response.setTopSongs(topSongs);

        return response;
    }

    public List<DailyListeningTime> getUserDailyListeningTime(String authorizationHeader, int days) {
        Auth currentUser = authService.getAuthFromAccessToken(authorizationHeader);
        return listeningHistoryRepository.getUserDailyListeningTime(currentUser.getId(), days);
    }


}
