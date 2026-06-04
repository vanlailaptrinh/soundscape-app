package com.example.soundscape_app.controller.admin;

import com.example.soundscape_app.dto.request.auth.RoleRequest;
import com.example.soundscape_app.dto.response.song.ListSongResponse;
import com.example.soundscape_app.dto.response.song.SongDetailResponse;
import com.example.soundscape_app.dto.response.user.ListUserResponse;
import com.example.soundscape_app.dto.response.user.UserDetailResponse;
import com.example.soundscape_app.service.song.SongService;
import com.example.soundscape_app.service.user.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final SongService songService;

    @GetMapping("/get-all-users")
    public Page<ListUserResponse> getAllUsers(Pageable pageable) {
        return adminService.getAllUsers(pageable);
    }

    @GetMapping("/get-user-detail/{userId}")
    public UserDetailResponse getUserDetail(@PathVariable Long userId) {
        return adminService.getUserDetail(userId);
    }

    @PutMapping("{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        String result = adminService.blockUser(userId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("{userId}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        String result = adminService.banUser(userId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        String result = adminService.unblockUser(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/roles/add")
    public ResponseEntity<String> addRoleToUser(
            @PathVariable Long userId,
            @RequestBody RoleRequest request) {
        String result = adminService.addRoleToUser(userId, request.getRole());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/roles/remove")
    public ResponseEntity<String> removeRoleFromUser(
            @PathVariable Long userId,
            @RequestBody RoleRequest request) {
        String result = adminService.removeRoleFromUser(userId, request.getRole());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-all-songs")
    public Page<ListSongResponse> getAllSongs(Pageable pageable) {
        return adminService.getAllSongs(pageable);
    }

    @GetMapping("/get-song-detail/{songId}")
    public SongDetailResponse getAllSongs(@PathVariable Long songId) {
        return songService.getSongDetail(songId);
    }

    @PutMapping("song/{songId}/ban")
    public ResponseEntity<String> banSong(@PathVariable Long songId) {
        String result = songService.bannedSong(songId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("song/{songId}/unblock")
    public ResponseEntity<String> unblockSong(@PathVariable Long songId) {
        String result = songService.unblockSong(songId);
        return ResponseEntity.ok(result);
    }

}
