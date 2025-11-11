package com.idear.backend.global.security.oauth.service;

import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.global.security.oauth.dto.CustomOAuth2User;
import com.idear.backend.global.security.oauth.dto.KakaoResponse;
import com.idear.backend.global.security.oauth.dto.OAuth2Response;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.domain.UserRole;
import com.idear.backend.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao

        OAuth2Response oAuth2Response = null;

        if (provider.equals("kakao")) {
            oAuth2Response = new KakaoResponse(attributes);
        }
        else {
            return null;
        }

        String name = oAuth2Response.getName();
        String email = oAuth2Response.getEmail();
        String providerInfo = oAuth2Response.getProviderInfo();

        // DB에 유저 없으면 생성
        User user = userRepository.findByProviderInfo(providerInfo)
                .orElseGet(() -> userRepository.save(User.of(name, email, providerInfo, UserRole.USER)));

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .providerInfo(user.getProviderInfo())
                .role(user.getRole())
                .build();

        return CustomOAuth2User.of(attributes, userInfo);
    }
}