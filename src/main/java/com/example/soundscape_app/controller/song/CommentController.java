package com.example.soundscape_app.controller.song;

import com.example.soundscape_app.dto.response.song.CommentAndUserResponse;
import com.example.soundscape_app.entity.song.Comment;
import com.example.soundscape_app.service.song.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //-------------------- User ---------------------//
    @PostMapping("user/comments/song/{songId}")
    public Comment addComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long songId,
            @RequestBody String content) {
        return commentService.addCommentToSong(authorizationHeader, songId, content);
    }

    @PostMapping("user/comments/reply/{parentId}")
    public CommentAndUserResponse addReply(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long parentId,
            @RequestBody String content) {
        return commentService.addReply(authorizationHeader, parentId, content);
    }

    @DeleteMapping("user/comments/{commentId}")
    public void deleteComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long commentId) {
        commentService.deleteComment(authorizationHeader, commentId);
    }

    @GetMapping("user/comments/song/{songId}")
    public List<CommentAndUserResponse> getComments(@PathVariable Long songId) {
        return commentService.getCommentsBySong(songId);
    }

    @GetMapping("user/comments/reply/{commentId}")
    public List<CommentAndUserResponse> getReplies(@PathVariable Long commentId) {
        return commentService.getReplies(commentId);
    }
}

