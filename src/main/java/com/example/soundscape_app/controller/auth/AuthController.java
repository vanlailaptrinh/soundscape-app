package com.example.soundscape_app.controller.auth;

import com.example.soundscape_app.dto.request.auth.LoginGoogleRequest;
import com.example.soundscape_app.dto.request.auth.LoginRequest;
import com.example.soundscape_app.dto.request.auth.RegisterRequest;
import com.example.soundscape_app.dto.request.auth.VerificationRequest;
import com.example.soundscape_app.dto.response.global.MessageResponse;
import com.example.soundscape_app.dto.response.user.AuthResponse;
import com.example.soundscape_app.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping("/register-verify")
    public AuthResponse verifyRegistration(@RequestParam("token") String token,
                                           HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return authService.completeRegistration(token, httpRequest, httpResponse
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
    public AuthResponse loginWithGoogle(@RequestBody LoginGoogleRequest request, HttpServletRequest
            httpRequest, HttpServletResponse httpResponse) {
        return authService.loginOrRegisterWithGoogle(request.getCode(), httpRequest, httpResponse);
    }

}
