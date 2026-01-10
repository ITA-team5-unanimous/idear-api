package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaVersion;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaWithVersionsResponse {
	private Long ideaId;
	private Long contestId;
	private String contestTitle;
	private LocalDateTime requestedAt;
	private String latestCertificateUrl;
	private List<VersionDetailInfo> versions;

	public static IdeaWithVersionsResponse of(Idea idea, List<IdeaVersion> versions, String latestCertificateUrl) {
		List<VersionDetailInfo> versionInfos = versions.stream()
				.map(VersionDetailInfo::of)
				.collect(Collectors.toList());

		Long contestId = null;
		String contestTitle = null;
		if (idea.getContest() != null) {
			contestId = idea.getContest().getContestId();
			contestTitle = idea.getContest().getTitle();
		}

		return IdeaWithVersionsResponse.builder()
				.ideaId(idea.getIdeaId())
				.contestId(contestId)
				.contestTitle(contestTitle)
				.requestedAt(idea.getRequestedAt())
				.latestCertificateUrl(latestCertificateUrl)
				.versions(versionInfos)
				.build();
	}
}
