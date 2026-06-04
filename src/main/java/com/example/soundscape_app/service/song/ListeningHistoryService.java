package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.response.song.DailyListeningTime;
import com.example.soundscape_app.dto.hepler.MonthlyPlayCount;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.ListeningHistory;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.repository.song.ListeningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListeningHistoryService {

    private final ListeningHistoryRepository listeningHistoryRepository;

    public void addListeningHistory(Auth auth, Song song) {
        ListeningHistory history = ListeningHistory.builder()
                .auth(auth)
                .song(song)
                .listenedAt(Instant.now())
                .build();

        listeningHistoryRepository.save(history);
    }

    // Stop / Next
    public Long updateListeningDuration(Auth auth, Song song) {
        if (auth == null || song == null) return 0L;

        var history = listeningHistoryRepository
                .findTopByAuthAndSongOrderByListenedAtDesc(auth, song)
                .orElse(null);
        if (history == null) return 0L;

        long delta = Duration.between(history.getListenedAt(), Instant.now()).getSeconds();
        delta = (long) Math.max(0, Math.min(delta, song.getDuration()));

        history.setDurationListened((double) delta);
        listeningHistoryRepository.save(history);
        return delta;
    }


    public ListeningHistory getListeningHistoryByUserAndSong(Auth auth, Song song) {
        return listeningHistoryRepository.findTopByAuthAndSongOrderByListenedAtDesc(auth, song)
                .orElseThrow(() -> new RuntimeException("No listening history found"));
    }

    public List<ListeningHistory> getRecentUniqueHistory(Long userId, int limit) {
        List<ListeningHistory> histories = listeningHistoryRepository
                .findTop100ByAuth_IdOrderByListenedAtDesc(userId);

        return histories.stream()
                .filter(h -> h.getSong() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(h -> h.getSong().getId(), h -> h, (a, b) -> a),
                        m -> m.values().stream()
                                .limit(limit)
                                .collect(Collectors.toList())
                ));
    }

    private List<Long> getMonthlyPlayCounts(Long artistId) {
        return listeningHistoryRepository
                .findMonthlyPlayCountsByArtist(artistId)
                .stream()
                .map(MonthlyPlayCount::getTotal)
                .toList();
    }

    public Long calculateAverageMonthlyListeners(Long artistId) {
        List<Long> counts = getMonthlyPlayCounts(artistId);

        if (counts.isEmpty()) return 0L;

        long total = counts.stream().mapToLong(Long::longValue).sum();
        return total / counts.size();
    }

    public Long getTotalPlayCountByUser(Long artistId) {
        List<Long> counts = getMonthlyPlayCounts(artistId);

        if (counts.isEmpty()) return 0L;

        return counts.stream().mapToLong(Long::longValue).sum();
    }

    public List<DailyListeningTime> getUserDailyListeningTime(Auth auth, int days) {
        return listeningHistoryRepository.getUserDailyListeningTime(auth.getId(), days);
    }

}