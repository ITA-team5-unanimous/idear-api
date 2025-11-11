package com.idear.backend.auth.infrastructure.repository;

import com.idear.backend.global.dto.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RefreshTokenRepository {

    @Value("${secret.jwt.refresh.expiration}")
    long refreshTokenExpiration;

    private final RedisTemplate<String, Object> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String refreshToken, UserInfo userInfo) {
        redisTemplate.opsForValue().set(refreshToken, userInfo);
        Duration duration = Duration.ofMillis(refreshTokenExpiration);
        redisTemplate.expire(refreshToken, duration);
    }

    public UserInfo getUserInfo(String refreshToken) {
        return (UserInfo) redisTemplate.opsForValue().get(refreshToken);
    }

    public boolean existsByRefresh(String refresh) {
        return redisTemplate.hasKey(refresh);
    }

    public void deleteByRefresh(String refresh) {
        redisTemplate.delete(refresh);
    }
}
