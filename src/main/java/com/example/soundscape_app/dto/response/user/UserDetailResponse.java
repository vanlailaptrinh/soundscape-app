package com.spotify.dto.response.user;

import com.spotify.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;
    private String username;
    private String email;
    private String urlAvatar;
    private Set<RoleEnum> role;
    private String status;
    private Date createdAt;

    private long totalSongs;
    private long totalListeningCount;
    private Long averageMonthlyListeners;
}