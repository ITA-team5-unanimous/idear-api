package com.idear.backend.user.dto.request;

import lombok.Getter;

@Getter
public class UpdateNotificationSettingsRequest {
	private Boolean push;
	private Boolean email;
}
