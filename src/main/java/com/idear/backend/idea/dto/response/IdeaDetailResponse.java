package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.idear.backend.idea.domain.Idea;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaDetailResponse {
	private Long ideaId;
	private String title;
	private String shortDescription;
	private String description;
	private LocalDateTime requestedAt;
	private List<IdeaFileInfo> files;
	private List<IdeaImageInfo> images;

	public static IdeaDetailResponse of(Idea idea) {
		List<IdeaFileInfo> fileInfos = idea.getFiles().stream()
				.map(IdeaFileInfo::of)
				.collect(Collectors.toList());

		List<IdeaImageInfo> imageInfos = idea.getImages().stream()
				.map(IdeaImageInfo::of)
				.collect(Collectors.toList());

		return IdeaDetailResponse.builder()
				.ideaId(idea.getIdeaId())
				.title(idea.getTitle())
				.shortDescription(idea.getShortDescription())
				.description(idea.getDescription())
				.requestedAt(idea.getRequestedAt())
				.files(fileInfos)
				.images(imageInfos)
				.build();
	}
}
