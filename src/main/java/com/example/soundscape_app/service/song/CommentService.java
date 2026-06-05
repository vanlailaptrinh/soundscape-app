package com.example.soundscape_app.service.song;

import com.example.soundscape_app.dto.response.song.CommentAndUserResponse;
import com.example.soundscape_app.dto.response.song.CommentResponse;
import com.example.soundscape_app.dto.response.user.UserResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.song.Comment;
import com.example.soundscape_app.entity.song.Song;
import com.example.soundscape_app.mapper.song.CommentMapper;
import com.example.soundscape_app.repository.song.CommentRepository;
import com.example.soundscape_app.repository.song.SongRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SongRepository songRepository;
    private final AuthService authService;
    private final CommentMapper commentMapper;
    private final UserService userService;

    @Transactional
    public Comment addCommentToSong(String authorizationHeader, Long songId, String content) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        Comment comment = new Comment();
        comment.setAuth(auth);
        comment.setSong(song);
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    @Transactional
    public CommentAndUserResponse addReply(String authorizationHeader, Long parentId, String content) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setAuth(auth);
        reply.setParent(parent);
        reply.setSong(parent.getSong());
        reply.setContent(content);

        Comment savedReply = commentRepository.save(reply);
        return mapToCommentAndUserResponse(savedReply, auth);
    }

    public List<CommentAndUserResponse> getCommentsBySong(Long songId) {
        List<Comment> comments = commentRepository.findBySongIdAndParentIsNullOrderByCreatedAtDesc(songId);
        return mapCommentListToResponse(comments);
    }

    public List<CommentAndUserResponse> getReplies(Long parentId) {
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtDesc(parentId);
        return mapCommentListToResponse(replies);
    }

    @Transactional
    public void deleteComment(String authorizationHeader, Long commentId) {
        Auth auth = authService.getAuthFromAccessToken(authorizationHeader);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuth().getId().equals(auth.getId())) {
            throw new RuntimeException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }


    private List<CommentAndUserResponse> mapCommentListToResponse(List<Comment> comments) {
        List<CommentAndUserResponse> responses = new ArrayList<>();
        for (Comment c : comments) {
            CommentAndUserResponse mapped = mapToCommentAndUserResponse(c, c.getAuth());
            if (mapped != null) responses.add(mapped);
        }
        return responses;
    }

    private CommentAndUserResponse mapToCommentAndUserResponse(Comment comment, Auth auth) {
        UserResponse user = userService.getUser(auth.getId());
        if (user == null) return null;

        CommentResponse commentResponse = commentMapper.toResponse(comment);
        CommentAndUserResponse response = new CommentAndUserResponse();
        response.setComment(commentResponse);
        response.setUser(user);
        return response;
    }
}
