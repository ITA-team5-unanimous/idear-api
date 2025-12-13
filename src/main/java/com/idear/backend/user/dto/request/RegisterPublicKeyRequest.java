package com.idear.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterPublicKeyRequest {
	@NotBlank(message = "퍼블릭 키는 필수입니다.")
	private String publicKey;
}
