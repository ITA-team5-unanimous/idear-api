package com.idear.backend.idea.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class IdeaCreateRequest {
	private Long contestId;
	@NotNull
	private String title;
	@NotNull
	private String shortDescription;
	@NotNull
	private String description;
}
