package com.example.soundscape_app.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {
    private String email;
    private String verificationCode;
}
