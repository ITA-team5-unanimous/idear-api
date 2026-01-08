package com.idear.backend.idea.dto.request;

import java.util.List;

import lombok.Getter;

@Getter
public class IdeaUpdateRequest {
	private List<Long> deleteFileIds;
	private List<Long> deleteImageIds;
	private String title;
	private String shortDescription;
	private String description;
	private String githubUrl;
	private String figmaUrl;
}
