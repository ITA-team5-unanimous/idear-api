package com.idear.backend.idea.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaRegistrationCompleteResponse {
	private Long ideaId;
	private List<FileRegistrationResult> registeredFiles;
	private Integer totalFiles;
}
