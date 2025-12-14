package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaRegistrationInitResponse {
	private Long ideaId;
	private LocalDateTime requestedAt;
	private List<FileHashInfo> files;
}
