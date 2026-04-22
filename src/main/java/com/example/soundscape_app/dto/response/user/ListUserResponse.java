package com.spotify.dto.response.user;

import com.spotify.enums.AccountStatusEnum;
import com.spotify.enums.RoleEnum;
import lombok.Data;

import java.util.Set;

@Data
public class ListUserResponse {
    private Long id;
    private String username;
    private String email;
    private Set<RoleEnum> roles;
    private AccountStatusEnum status;
}
