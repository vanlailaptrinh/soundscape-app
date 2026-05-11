package com.example.soundscape_app.dto.request.auth;


import com.spotify.enums.RoleEnum;
import lombok.Data;

@Data
public class RoleRequest {
    private RoleEnum role;
}