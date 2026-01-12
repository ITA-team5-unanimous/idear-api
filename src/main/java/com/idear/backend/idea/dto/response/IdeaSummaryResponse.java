package com.idear.backend.idea.dto.response;

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
	private String contestTitle;
	private String ideaTitle;
	private String host;
	private Long dday;
	private String contestImageUrl;
	private LocalDateTime requestedAt;
	private List<IdeaImageInfo> images;

	public static IdeaSummaryResponse of(Idea idea, IdeaVersion version) {
		List<IdeaImageInfo> imageResponses = version.getImages().stream()
				.map(IdeaImageInfo::of)
				.collect(Collectors.toList());

		String contestTitle = null;
		String host = null;
		Long dDay = null;
		String contestImageUrl = null;
		if (idea.getContest() != null) {
			contestTitle = idea.getContest().getTitle();
			host = idea.getContest().getHost();
			dDay = idea.getContest().getDDay();
			contestImageUrl = idea.getContest().getImageUrl();
		}

		return IdeaSummaryResponse.builder()
				.ideaId(idea.getIdeaId())
				.ideaVersionId(version.getIdeaVersionId())
				.versionNumber(version.getVersionNumber())
				.contestTitle(contestTitle)
				.ideaTitle(version.getTitle())
				.host(host)
				.dday(dDay)
				.contestImageUrl(contestImageUrl)
				.requestedAt(version.getRequestedAt())
				.images(imageResponses)
				.build();
	}
}
