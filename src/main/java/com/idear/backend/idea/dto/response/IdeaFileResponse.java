package com.idear.backend.idea.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaFileResponse {
	private Long fileId;
	private String fileName;
	private String fileType;
	private String filePath;
}
