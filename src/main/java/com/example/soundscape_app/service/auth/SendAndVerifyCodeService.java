package com.spotify.service.auth;

import com.spotify.util.EmailUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class SendAndVerifyCodeService {
    private final StringRedisTemplate redisTemplate;
    private static final long EXPIRATION_TIME = 5; // 5 minutes
    private final EmailUtil emailUtil;


    public void sendVerificationCode(String email) {
        String code = generateVerificationCode();
        redisTemplate.opsForValue().set(getRedisKey(email), code, EXPIRATION_TIME, TimeUnit.MINUTES);
        emailUtil.sendEmail(email, code);
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(getRedisKey(email));
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(getRedisKey(email));
            return true;
        }
        return false;
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
    
    private String getRedisKey(String email) {
        return "verification_code:" + email;
    }


}
