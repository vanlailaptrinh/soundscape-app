package com.example.soundscape_app.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginGoogleRequest {
    private String code;
   
}
