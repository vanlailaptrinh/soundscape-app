package com.spotify.service.auth;

import com.spotify.entity.auth.Auth;
import com.spotify.entity.auth.RefreshToken;
import com.spotify.repository.auth.AuthRepository;
import com.spotify.repository.auth.RefreshTokenRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.expiration}")
    private String refreshTokenDurationMsStr;  // Inject dạng String

    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @PostConstruct
    public void init() {
        this.refreshTokenDurationMs = Long.parseLong(refreshTokenDurationMsStr);
    }

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AuthRepository authRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Auth auth, String deviceInfo) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAuth(auth);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setDeviceId(deviceInfo);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUserIdAndDeviceId(Auth auth, String deviceId) {
        refreshTokenRepository.deleteByAuthAndDeviceId(auth, deviceId);
    }

    public boolean validateToken(String token) {
        return findByToken(token)
                .map(refreshToken -> !refreshToken.getExpiryDate().isBefore(Instant.now()))
                .orElse(false);
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }


}
