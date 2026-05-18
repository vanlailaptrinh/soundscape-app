package com.example.soundscape_app.dto.response.user;

import com.example.soundscape_app.enums.AccountStatusEnum;
import com.example.soundscape_app.enums.RoleEnum;
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
