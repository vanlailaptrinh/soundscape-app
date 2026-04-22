package com.spotify.service.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {
    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationTimeMillis) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTimeMillis, TimeUnit.MILLISECONDS);
    }
    
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
