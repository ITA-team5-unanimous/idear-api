package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaUpdateResponse {
	private Long ideaId;
	private Integer versionNumber;
	private LocalDateTime updatedAt;
	private List<FileHashInfo> files;
}
