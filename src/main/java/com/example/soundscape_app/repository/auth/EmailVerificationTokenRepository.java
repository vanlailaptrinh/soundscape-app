package com.example.soundscape_app.repository.auth;

import com.example.soundscape_app.entity.auth.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findAllByUser_IdAndConsumedAtIsNullAndExpiresAtAfter(Long userId, Instant now);
}
