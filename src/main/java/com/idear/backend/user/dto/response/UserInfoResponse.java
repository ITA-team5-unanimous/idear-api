package com.idear.backend.user.dto.response;

import com.idear.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
	private String name;
	private String email;
	private String provider;

	public static UserInfoResponse from(User user) {
		return UserInfoResponse.builder()
				.name(user.getName())
				.email(user.getEmail())
				.provider(user.getProvider())
				.build();
	}
}
