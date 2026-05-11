package com.example.soundscape_app.repository.auth;

import com.example.soundscape_app.entity.auth.Auth;
import com.example.soundscape_app.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByAuthAndDeviceId(Auth auth, String deviceId);
}
