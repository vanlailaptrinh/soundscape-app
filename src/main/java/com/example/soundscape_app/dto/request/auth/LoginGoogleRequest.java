package com.example.soundscape_app.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginGoogleRequest {
    private String code;
}
