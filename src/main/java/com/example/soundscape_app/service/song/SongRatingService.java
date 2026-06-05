package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.request.song.SongRatingRequest;
import com.example.soundscape_app.dto.response.song.SongRatingStatsResponse;
import com.example.soundscape_app.dto.response.song.SongResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.entity.song.SongRating;
import com.example.soundscape_app.mapper.song.SongMapper;
import com.example.soundscape_app.repository.song.SongRatingRepository;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongRatingService {

    private final SongRepository songRepository;
    private final SongRatingRepository songRatingRepository;
    private final AuthService authService;
    private final SongMapper songMapper;

    @Transactional
    public SongResponse rateSong(SongRatingRequest songRatingRequest, String authorizationHeader) {
        Long songId = songRatingRequest.getSongId();
        int rating = songRatingRequest.getRating();
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating must be from 1 to 5");

        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        Optional<SongRating> exist = songRatingRepository.findBySongAndUser(song, user);

        if (exist.isPresent()) {
            exist.get().setRating(rating);
        } else {
            SongRating r = SongRating.builder()
                    .song(song)
                    .user(user)
                    .rating(rating)
                    .build();
            songRatingRepository.save(r);
            song.setRatingCount(song.getRatingCount() + 1);
        }

        updateSongAverage(song);
        songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    @Transactional
    public SongResponse unrateSong(Long songId, String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        var exist = songRatingRepository.findBySongAndUser(song, user)
                .orElseThrow(() -> new RuntimeException("User chưa đánh giá bài hát này"));

        songRatingRepository.delete(exist);
        song.setRatingCount(Math.max(0, song.getRatingCount() - 1));

        updateSongAverage(song);
        songRepository.save(song);
        return songMapper.toSongResponse(song);
    }

    public Integer getUserRating(Long songId, String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        return songRatingRepository.findBySongAndUser(song, user)
                .map(SongRating::getRating)
                .orElse(null);
    }

    public Double getAverageRating(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        return song.getRatingAvg();
    }

    private void updateSongAverage(Song song) {
        List<SongRating> ratings = songRatingRepository.findAllBySong(song);
        double avg = ratings.stream()
                .mapToInt(SongRating::getRating)
                .average()
                .orElse(0.0);
        song.setRatingAvg(avg);
    }

    public SongRatingStatsResponse getRatingStats(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        List<SongRating> ratings = songRatingRepository.findAllBySong(song);

        Map<Integer, Long> distribution = ratings.stream()
                .collect(Collectors.groupingBy(
                        SongRating::getRating,
                        Collectors.counting()
                ));

        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        return SongRatingStatsResponse.builder()
                .average(song.getRatingAvg())
                .total((long) song.getRatingCount())
                .distribution(distribution)
                .build();
    }
}

