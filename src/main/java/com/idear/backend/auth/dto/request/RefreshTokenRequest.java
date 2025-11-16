package com.idear.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {
	@NotBlank(message = "리프레시 토큰 문자열은 필수입니다.")
	private String refresh;
}
