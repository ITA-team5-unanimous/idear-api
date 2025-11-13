package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.idear.backend.idea.domain.Idea;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaResponse {
	private Long ideaId;
	private String title;
	private String shortDescription;
	private String description;
	//private String proofHash;
	private Idea.IdeaStatus status;
	private LocalDateTime createdAt;
	private List<IdeaFileResponse> files;
}
