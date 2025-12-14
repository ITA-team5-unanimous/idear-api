package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.idear.backend.idea.domain.Idea;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaSummaryResponse {
	private Long ideaId;
	private String title;
	private String shortDescription;
	private LocalDateTime requestedAt;
	private List<IdeaImageInfo> images;

	public static IdeaSummaryResponse of(Idea idea) {
		List<IdeaImageInfo> imageResponses = idea.getImages().stream()
				.map(IdeaImageInfo::of)
				.collect(Collectors.toList());

		return IdeaSummaryResponse.builder()
				.ideaId(idea.getIdeaId())
				.title(idea.getTitle())
				.shortDescription(idea.getShortDescription())
				.requestedAt(idea.getRequestedAt())
				.images(imageResponses)
				.build();
	}
}
