package com.example.soundscape_app.dto.request.auth;


import com.example.soundscape_app.enums.RoleEnum;
import lombok.Data;

@Data
public class RoleRequest {
    private RoleEnum role;
}