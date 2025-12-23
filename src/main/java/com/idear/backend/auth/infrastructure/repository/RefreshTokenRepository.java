package com.idear.backend.auth.infrastructure.repository;

import com.idear.backend.global.dto.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RefreshTokenRepository {

    private static final String REFRESH_TOKEN_PREFIX = "api:refresh:";

    @Value("${secret.jwt.refresh.expiration}")
    long refreshTokenExpiration;

    private final RedisTemplate<String, Object> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String refreshToken, UserInfo userInfo) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, userInfo);
        Duration duration = Duration.ofMillis(refreshTokenExpiration);
        redisTemplate.expire(key, duration);
    }

    public UserInfo getUserInfo(String refreshToken) {
        return (UserInfo) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
    }

    public boolean existsByRefresh(String refresh) {
        return redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refresh);
    }

    public void deleteByRefresh(String refresh) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + refresh);
    }
}
