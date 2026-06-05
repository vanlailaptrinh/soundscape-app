package com.example.soundscape_app.controller.user;

import com.example.soundscape_app.dto.request.song.FeedbackRequest;
import com.example.soundscape_app.entity.song.Feedback;
import com.example.soundscape_app.service.user.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    //-------------------- User ---------------------//

    @PostMapping("/user/feedbacks")
    public ResponseEntity<Feedback> addFeedback(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody FeedbackRequest request) {
        Feedback feedback = feedbackService.saveFeedback(authorizationHeader, request);
        return ResponseEntity.ok(feedback);
    }
}