package com.spotify.controller.auth;

import com.spotify.dto.request.auth.LoginRequest;
import com.spotify.dto.request.auth.RegisterRequest;
import com.spotify.dto.request.auth.VerificationRequest;
import com.spotify.dto.response.global.MessageResponse;
import com.spotify.dto.response.user.AuthResponse;
import com.spotify.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        return authService.login(request, httpRequest, httpResponse);
    }

    @PostMapping("/register-initiate")
    public MessageResponse registerInitiate(@RequestBody RegisterRequest request) {
        return authService.initiateRegistration(request);
    }

    @PostMapping("/register-verify")
    public AuthResponse verifyRegistration(@RequestBody VerificationRequest verificationCode,
                                           HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return authService.completeRegistration(verificationCode.getEmail(),
                verificationCode.getVerificationCode(), httpRequest, httpResponse
        );
    }

    @PostMapping("/logout")
    @Transactional
    public MessageResponse logout(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
            , HttpServletRequest request) {
        return authService.logout(authorizationHeader, request);
    }

    @GetMapping("/access-token")
    public AuthResponse getAccessToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }

    @PostMapping(value = "google-callback")
    public AuthResponse loginWithGoogle(@RequestBody String code, HttpServletRequest
            httpRequest, HttpServletResponse httpResponse) {
        return authService.loginOrRegisterWithGoogle(code, httpRequest, httpResponse);
    }

}
