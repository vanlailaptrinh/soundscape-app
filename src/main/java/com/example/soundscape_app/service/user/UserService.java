package com.example.soundscape_app.service.user;


import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.response.user.UserResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.service.auth.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AuthService authService;

    public UserResponse getUser(String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUser(Long userId) {
        Auth user = authService.getById(userId);
        return userMapper.toUserResponse(user);
    }

}
