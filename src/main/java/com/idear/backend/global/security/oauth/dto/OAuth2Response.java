package com.idear.backend.global.security.oauth.dto;

public interface OAuth2Response {

    // Provider (kakao, naver 등)
    String getProvider();

    // Provider 발급 아이디
    String getProviderId();

    // 서드파티 유일 식별자
    String getProviderInfo();

    String getEmail();

    String getName();
}