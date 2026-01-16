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
	private String publicKey;
	private String profileImageUrl;

	public static UserInfoResponse from(User user, String defaultImageUrl) {
		String profileImageUrl = user.getProfileImageUrl();
		if (profileImageUrl == null || profileImageUrl.isBlank()) {
			profileImageUrl = defaultImageUrl;
		}

		return UserInfoResponse.builder()
				.name(user.getName())
				.email(user.getEmail())
				.provider(user.getProvider())
				.publicKey(user.getPublicKey())
				.profileImageUrl(profileImageUrl)
				.build();
	}
}
