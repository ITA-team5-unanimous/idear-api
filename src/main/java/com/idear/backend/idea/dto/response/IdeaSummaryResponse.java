package com.idear.backend.idea.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class IdeaSummaryResponse {
	private Long ideaId;
	private Long ideaVersionId;
	private Integer versionNumber;
	private String title;
	private String host;
	private Long dday;
	private String contestImageUrl;
	private String shortDescription;
	private String githubUrl;
	private String figmaUrl;
	private LocalDateTime requestedAt;
	private List<IdeaImageInfo> images;

	public static IdeaSummaryResponse of(Idea idea, IdeaVersion version) {
		List<IdeaImageInfo> imageResponses = version.getImages().stream()
				.map(IdeaImageInfo::of)
				.collect(Collectors.toList());

		String host = null;
		Long dDay = null;
		String contestImageUrl = null;
		if (idea.getContest() != null) {
			host = idea.getContest().getHost();
			dDay = idea.getContest().getDDay();
			contestImageUrl = idea.getContest().getImageUrl();
		}

		return IdeaSummaryResponse.builder()
				.ideaId(idea.getIdeaId())
				.ideaVersionId(version.getIdeaVersionId())
				.versionNumber(version.getVersionNumber())
				.title(idea.getTitle())
				.host(host)
				.dday(dDay)
				.contestImageUrl(contestImageUrl)
				.shortDescription(version.getShortDescription())
				.githubUrl(version.getGithubUrl())
				.figmaUrl(version.getFigmaUrl())
				.requestedAt(version.getRequestedAt())
				.images(imageResponses)
				.build();
	}
}
