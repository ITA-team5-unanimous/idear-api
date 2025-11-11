package com.idear.backend.global.security.oauth.dto;

import com.idear.backend.global.dto.UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final UserInfo userInfo;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(userInfo.role().toRoleString()));
    }

    @Override
    public String getName() {
        return this.userInfo.name();
    }

    public static CustomOAuth2User of(Map<String, Object> attributes, UserInfo userInfo) {
        return new CustomOAuth2User(attributes, userInfo);
    }
}