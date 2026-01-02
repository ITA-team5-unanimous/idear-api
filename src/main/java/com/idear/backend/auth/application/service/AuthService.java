package com.idear.backend.auth.application.service;

import com.idear.backend.auth.infrastructure.repository.RefreshTokenRepository;
import com.idear.backend.auth.dto.response.TokenResponse;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.security.token.TokenProvider;
import com.idear.backend.global.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.idear.backend.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!refreshTokenRepository.existsByRefresh(refreshToken)) {
            throw CustomException.of(INVALID_REFRESH_TOKEN);
        }

        UserInfo userInfo = refreshTokenRepository.getUserInfo(refreshToken);
        if (userInfo == null) {
            throw CustomException.of(INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.deleteByRefresh(refreshToken);

        String newAccessToken = tokenProvider.generateAccessToken(userInfo);
        String newRefreshToken = tokenProvider.generateRefreshToken(userInfo);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
