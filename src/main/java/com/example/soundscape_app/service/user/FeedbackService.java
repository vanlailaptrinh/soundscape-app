package com.example.soundscape_app.service.user;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Feedback;
import com.example.soundscape_app.entity.song.ListeningHistory;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.repository.song.FeedbackRepository;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.song.ListeningHistoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SongRepository songRepository;
    private final AuthService authService;
    private final ListeningHistoryService listeningHistoryService;

    @Transactional
    public Feedback saveFeedback(String authorizationHeader, FeedbackRequest request) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        Song song = songRepository.findById(request.getSongId()).orElseThrow(() -> new RuntimeException("Song not found"));

        List<Long> historyIds = listeningHistoryService.getRecentUniqueHistory(user.getId(), 10)
                .stream()
                .map(ListeningHistory::getId)
                .collect(Collectors.toList());

        Feedback feedback = Feedback.builder()
                .user(user)
                .song(song)
                .rating(request.getRating())
                .source(request.getSource())
                .listeningHistoryIds(historyIds)
                .build();

        return feedbackRepository.save(feedback);
    }
}
