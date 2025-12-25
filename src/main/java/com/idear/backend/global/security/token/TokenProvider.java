package com.idear.backend.global.security.token;

import com.idear.backend.auth.infrastructure.repository.RefreshTokenRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.user.domain.UserRole;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.idear.backend.global.exception.ErrorCode.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class TokenProvider {

    private static final String BEARER = "Bearer ";

    @Value("${secret.jwt.key}")
    private String secretKey;

    @Value("${secret.jwt.access.expiration}")
    private long accessTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public String generateRefreshToken(UserInfo userInfo) {
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.saveRefreshToken(refreshToken, userInfo);
        return refreshToken;
    }

    public String generateAccessToken(UserInfo userInfo) {
        final Claims claims = Jwts.claims();
        claims.put("sub", String.valueOf(userInfo.id()));
        claims.put("role", String.valueOf(userInfo.role()));

        Date now = new Date(System.currentTimeMillis());
        Date expiredAt = new Date(System.currentTimeMillis()+accessTokenExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER)) {
            return authorization.substring(BEARER.length());
        }
        return null;
    }

    public String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("iDear_admin_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Authentication validateToken(String accessToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken);
            Claims body = claims.getBody();

            return getAuthentication(body);
        } catch (ExpiredJwtException e) {
            throw CustomException.of(EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw CustomException.of(INVALID_ACCESS_TOKEN, "잘못된 형식의 토큰입니다");
        } catch (SignatureException | SecurityException e) {
            throw CustomException.of(INVALID_ACCESS_TOKEN, "유효하지 않은 서명입니다");
        } catch (IllegalArgumentException e) {
            throw CustomException.of(INVALID_ACCESS_TOKEN, "유효하지 않은 토큰입니다");
        }
    }

    private Authentication getAuthentication(Claims claims) {
        String userIdStr = claims.get("sub", String.class);
        Long userId = Long.parseLong(userIdStr);

        String roleStr = claims.get("role", String.class);
        UserRole role = UserRole.valueOf(roleStr);

        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role.toRoleString())
        );

        UserInfo userInfo = new UserInfo(userId, null, null, null, role);

        return new UsernamePasswordAuthenticationToken(userInfo, null, authorities);
    }
}
