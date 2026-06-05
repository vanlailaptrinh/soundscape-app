package com.example.soundscape_app.dto.response.song;

import com.example.soundscape_app.dto.response.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAndUserResponse {
    private CommentResponse comment;
    private UserResponse user;
}
