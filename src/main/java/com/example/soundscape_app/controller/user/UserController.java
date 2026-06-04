package com.example.soundscape_app.controller.user;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.example.soundscape_app.dto.response.user.UserResponse;
import com.example.soundscape_app.service.user.SearchService;
import com.example.soundscape_app.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final SearchService searchService;

    @GetMapping("/user/profile")
    public UserResponse getUserProfile(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return userService.getUser(authorizationHeader);
    }

    @GetMapping("/search")
    public Map<String, Object> searchAll(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Map.of("results", Collections.emptyList());
        }
        return searchService.searchAll(keyword.trim());
    }
}