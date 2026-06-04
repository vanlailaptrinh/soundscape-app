package com.example.soundscape_app.service.user;


import org.springframework.stereotype.Service;

import com.example.soundscape_app.dto.request.auth.UpdateProfileRequest;
import com.example.soundscape_app.dto.response.user.UserResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.repository.auth.AuthRepository;
import com.example.soundscape_app.service.auth.AuthService;
import com.example.soundscape_app.util.S3Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AuthService authService;
    private final AuthRepository authRepository;
    private final S3Util s3Util;

    public UserResponse getUser(String authorizationHeader) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUser(Long userId) {
        Auth user = authService.getById(userId);
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateProfile(String authorizationHeader, UpdateProfileRequest request) {
        Auth user = authService.getAuthFromAccessToken(authorizationHeader);

        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            user.setDescription(request.getDescription().trim());
        }

        // Upload and update avatar if provided
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = s3Util.uploadFileImage(request.getAvatar());
            user.setUrlAvatar(avatarUrl);
        }

        Auth savedUser = authRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

}
