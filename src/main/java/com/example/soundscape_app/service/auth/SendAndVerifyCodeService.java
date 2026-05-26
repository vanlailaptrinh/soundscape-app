package com.example.soundscape_app.service.auth;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.EmailVerificationToken;
import com.example.soundscape_app.exception.BadRequestException;
import com.example.soundscape_app.repository.auth.AuthRepository;
import com.example.soundscape_app.repository.auth.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;


@RequiredArgsConstructor
@Service
public class SendAndVerifyCodeService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final AuthRepository appUserRepository;
    private final EmailService emailService;

    @Value("${app.web.base-url:http://localhost:8081}")
    private String webBaseUrl;

    @Value("${app.email.verification-token-ttl:PT24H}")
    private Duration verificationTokenTtl;

    @Transactional
    public boolean sendVerificationEmail(Auth user) {
        String rawToken = generateToken();
        String tokenHash = sha256Hex(rawToken);
        Instant expiresAt = Instant.now().plus(verificationTokenTtl);

        // Revoke any existing active tokens for this user to keep verification single-use.
        Instant now = Instant.now();
        for (EmailVerificationToken active : tokenRepository.findAllByUser_IdAndConsumedAtIsNullAndExpiresAtAfter(user.getId(), now)) {
            active.setConsumedAt(now);
        }

        tokenRepository.save(new EmailVerificationToken(user, tokenHash, expiresAt));

        String verifyLink = webBaseUrl + "/api/auth/register-verify?token=" + rawToken;
        String subject = "Verify your email";
        String html = buildHtml(user.getUsername(), verifyLink);
        try {
            emailService.sendHtml(user.getEmail(), subject, html);
            return true;
        } catch (RuntimeException ex) {
            if (failOnError) {
                throw ex;
            }
            return false;
        }
    }

    @Transactional
    public Auth verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Verification token is required");
        }

        String tokenHash = sha256Hex(rawToken.trim());
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Verification token is invalid"));

        Instant now = Instant.now();
        if (token.isConsumed()) {
            throw new BadRequestException("Verification token was already used");
        }
        if (token.isExpired(now)) {
            throw new BadRequestException("Verification token is expired");
        }

        Auth user = appUserRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new BadRequestException("User not found for verification token"));

        token.setConsumedAt(now);
        appUserRepository.save(user);
        tokenRepository.save(token);

        return user;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String value) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String buildHtml(String username, String verifyLink) {
        String safeUsername = username == null ? "Listener" : escapeHtml(username);

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Email</title>
            </head>

            <body style="
                margin:0;
                padding:0;
                background:#000000;
                font-family:Arial,sans-serif;
                color:#ffffff;
            ">

                <div style="
                    width:100%%;
                    padding:50px 20px;
                    background:
                        radial-gradient(circle at top left, rgba(30,215,96,0.25), transparent 35%%),
                        radial-gradient(circle at bottom right, rgba(30,215,96,0.15), transparent 30%%),
                        #000000;
                ">

                    <div style="
                        max-width:620px;
                        margin:0 auto;
                        background:#121212;
                        border-radius:28px;
                        overflow:hidden;
                        border:1px solid rgba(255,255,255,0.06);
                        box-shadow:0 0 40px rgba(30,215,96,0.15);
                    ">

                        <div style="
                            padding:50px 40px 30px;
                            text-align:center;
                            background:
                                linear-gradient(
                                    180deg,
                                    rgba(30,215,96,0.18),
                                    rgba(18,18,18,1)
                                );
                        ">

                            <div style="
                                width:90px;
                                height:90px;
                                margin:0 auto 24px;
                                border-radius:50%%;
                                background:#1ed760;
                                text-align: center;
                                line-height: 90px;
                                box-shadow:0 0 30px rgba(30,215,96,0.6);
                            ">
                               <img src="https://img.icons8.com/ios-filled/50/000000/spotify.png" 
                                       width="42" 
                                       height="42" 
                                       alt="Spotify"
                                       style="display: inline-block; vertical-align: middle; margin: 0;"/>
                            </div>

                            <h1 style="
                                margin:0;
                                font-size:38px;
                                font-weight:800;
                                letter-spacing:1px;
                                color:#ffffff;
                            ">
                                Musicify
                            </h1>

                            <p style="
                                margin-top:14px;
                                color:#b3b3b3;
                                font-size:16px;
                                line-height:1.6;
                            ">
                                Stream your vibe. Discover your sound.
                            </p>

                        </div>

                        <div style="padding:45px 40px;">

                            <p style="
                                margin:0 0 18px;
                                font-size:20px;
                                color:#ffffff;
                            ">
                                Hi <strong style="color:#1ed760;">%s</strong>,
                            </p>

                            <p style="
                                margin:0;
                                color:#b3b3b3;
                                font-size:15px;
                                line-height:1.8;
                            ">
                                You're one step away from unlocking millions of tracks,
                                curated playlists, and immersive listening experiences.
                                Verify your email to activate your account.
                            </p>

                            <div style="text-align:center;margin:42px 0;">

                                <a href="%s" style="
                                    display:inline-block;
                                    padding:18px 40px;
                                    background:#1ed760;
                                    color:#000000;
                                    text-decoration:none;
                                    border-radius:999px;
                                    font-size:16px;
                                    font-weight:800;
                                    letter-spacing:0.5px;
                                    box-shadow:0 0 25px rgba(30,215,96,0.45);
                                ">
                                    VERIFY EMAIL
                                </a>

                            </div>

                            <div style="
                                background:#181818;
                                border-radius:18px;
                                padding:22px;
                                border:1px solid rgba(255,255,255,0.05);
                            ">

                                <p style="
                                    margin:0 0 10px;
                                    color:#1ed760;
                                    font-size:14px;
                                    font-weight:700;
                                ">
                                    Why verify?
                                </p>

                                <ul style="
                                    padding-left:18px;
                                    margin:0;
                                    color:#d1d5db;
                                    line-height:1.8;
                                    font-size:14px;
                                ">
                                    <li>Create and save playlists</li>
                                    <li>Get personalized recommendations</li>
                                    <li>Sync music across devices</li>
                                    <li>Follow artists and podcasts</li>
                                </ul>

                            </div>

                            <p style="
                                margin-top:28px;
                                color:#6b7280;
                                font-size:13px;
                                line-height:1.7;
                            ">
                                If you didn’t sign up for Musicify,
                                you can safely ignore this email.
                            </p>

                        </div>

                        <div style="
                            padding:24px;
                            text-align:center;
                            background:#0b0b0b;
                            border-top:1px solid rgba(255,255,255,0.05);
                        ">

                            <p style="
                                margin:0;
                                color:#6b7280;
                                font-size:12px;
                                letter-spacing:0.5px;
                            ">
                                © 2026 spotify • All rights reserved
                            </p>

                        </div>

                    </div>

                </div>

            </body>
            </html>
            """.formatted(safeUsername, verifyLink);
    }



    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Value("${app.mail.fail-on-error:false}")
    private boolean failOnError;


}
