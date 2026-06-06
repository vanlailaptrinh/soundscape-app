package com.example.soundscape_app.service.user;

import com.example.soundscape_app.dto.response.song.ListSongResponse;
import com.example.soundscape_app.dto.response.song.AppListeningStatsResponse;
import com.example.soundscape_app.dto.response.user.ListUserResponse;
import com.example.soundscape_app.dto.response.user.UserDetailResponse;
import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.Role;
import com.example.soundscape_app.enums.AccountStatusEnum;
import com.example.soundscape_app.enums.RoleEnum;
import com.example.soundscape_app.mapper.song.UserMapper;
import com.example.soundscape_app.repository.auth.AuthRepository;
import com.example.soundscape_app.service.auth.RoleService;
import com.example.soundscape_app.service.song.ListeningHistoryService;
import com.example.soundscape_app.service.song.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuthRepository authRepository;
    private final ListeningHistoryService listeningHistoryService;
    private final SongService songService;
    private final UserMapper userMapper;
    private final RoleService roleService;

    public Page<ListUserResponse> getAllUsers(Pageable pageable) {

        Sort sort = pageable.getSort();
        if (!sort.iterator().hasNext()) {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Auth> authPage = authRepository.findAll(sortedPageable);

        return authPage.map(userMapper::toListUserResponse);
    }

    public UserDetailResponse getUserDetail(Long userId) {

        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalSongs = songService.getTotalSongsByUser(userId);
        long totalListening = songService.getTotalPlayCountByUser(userId);
        Long averageMonthlyListeners = listeningHistoryService.calculateAverageMonthlyListeners(userId);

        return userMapper.toUserDetailResponse(
                user,
                totalSongs,
                totalListening,
                averageMonthlyListeners
        );
    }

    private String updateUserStatus(Long userId, AccountStatusEnum status) {
        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(status);
        authRepository.save(user);

        return "User " + user.getUsername() + " status updated to " + status;
    }

    public String blockUser(Long userId) {
        return updateUserStatus(userId, AccountStatusEnum.LOCKED);
    }

    public String banUser(Long userId) {
        return updateUserStatus(userId, AccountStatusEnum.BANNED);
    }

    public String unblockUser(Long userId) {
        return updateUserStatus(userId, AccountStatusEnum.ACTIVE);
    }

    public String addRoleToUser(Long userId, RoleEnum roleEnum) {

        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleService.getRole(roleEnum);

        if (roleService.userHasRole(user, roleEnum)) {
            return "User already has role: " + roleEnum;
        }

        user.getRoleEntities().add(role);
        authRepository.save(user);
        return "Added role " + roleEnum + " to user " + user.getUsername();
    }

    public String removeRoleFromUser(Long userId, RoleEnum roleEnum) {

        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleService.getRole(roleEnum);

        if (!roleService.userHasRole(user, roleEnum)) {
            return "User does not contain role: " + roleEnum;
        }

        user.getRoleEntities().remove(role);
        authRepository.save(user);

        return "Removed role " + roleEnum + " from user " + user.getUsername();
    }

    public Page<ListSongResponse> getAllSongs(Pageable pageable) {

        Sort sort = pageable.getSort();
        if (!sort.iterator().hasNext()) {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        return songService.getAllSongs(sortedPageable);
    }

    public AppListeningStatsResponse getAppListeningStats(int days) {
        return listeningHistoryService.getAppListeningStats(days);
    }

}

