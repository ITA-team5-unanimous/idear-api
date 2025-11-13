package com.idear.backend.idea.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class IdeaRegisterRequest {
	@NotNull
	private Long userId;
	@NotNull
	private Long contestId;
	@NotNull
	private String title;
	@NotNull
	private String shortDescription;
	@NotNull
	private String description;
}
