package com.idear.backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
	private String access;
	private String refresh;

    public static TokenResponse of(String access, String refresh) {
        return TokenResponse.builder()
                .access(access)
                .refresh(refresh)
                .build();
    }
}
