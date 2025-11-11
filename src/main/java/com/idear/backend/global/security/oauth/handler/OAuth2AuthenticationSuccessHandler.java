package com.idear.backend.global.security.oauth.handler;

import com.idear.backend.global.security.oauth.dto.CustomOAuth2User;
import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.global.security.token.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${spring.front.origin}")
    private String frontDomain;

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        CustomOAuth2User oauth2User = (CustomOAuth2User) authToken.getPrincipal();

        UserInfo userInfo = oauth2User.getUserInfo();
        String refreshToken = tokenProvider.generateRefreshToken(userInfo);

        response.sendRedirect(frontDomain+"?refresh="+refreshToken);
    }
}