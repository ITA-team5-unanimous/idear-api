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
	private String title;
	private Long contestId;
	private LocalDateTime requestedAt;
	private List<VersionDetailInfo> versions;

	public static IdeaWithVersionsResponse of(Idea idea, List<IdeaVersion> versions) {
		List<VersionDetailInfo> versionInfos = versions.stream()
				.map(VersionDetailInfo::of)
				.collect(Collectors.toList());

		return IdeaWithVersionsResponse.builder()
				.ideaId(idea.getIdeaId())
				.title(idea.getTitle())
				.contestId(idea.getContest() != null ? idea.getContest().getContestId() : null)
				.requestedAt(idea.getRequestedAt())
				.versions(versionInfos)
				.build();
	}
}
