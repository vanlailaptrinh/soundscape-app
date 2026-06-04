package com.example.soundscape_app.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.soundscape_app.dto.request.song.FollowRequest;
import com.example.soundscape_app.dto.request.song.UnFollowRequest;
import com.example.soundscape_app.dto.response.album.AlbumResponse;
import com.example.soundscape_app.dto.response.user.ArtistResponse;
import com.example.soundscape_app.dto.response.user.FollowedResponse;
import com.example.soundscape_app.service.song.FollowService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    //-------------------- User ---------------------//

    @PostMapping("user/follow")
    public ResponseEntity<String> follow(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody FollowRequest request) {
        return ResponseEntity.ok(followService.follow(authorizationHeader, request));
    }

    @PostMapping("user/unfollow")
    public ResponseEntity<String> unfollow(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody UnFollowRequest request) {
        return ResponseEntity.ok(followService.unfollow(authorizationHeader, request));
    }

    @GetMapping("user/followed")
    public ResponseEntity<FollowedResponse> getFollowed(
            @RequestHeader("Authorization") String authorizationHeader) {
        FollowedResponse response = followService.followed(authorizationHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("user/followedArtist")
    public ResponseEntity<List<ArtistResponse>> followedArtist(
            @RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(followService.followedArtist(authorizationHeader));
    }

    @GetMapping("user/followedAlbum")
    public ResponseEntity<List<AlbumResponse>> followedAlbum(
            @RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(followService.followedAlbum(authorizationHeader));
    }
}
