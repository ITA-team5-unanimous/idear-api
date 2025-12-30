package com.idear.backend.idea.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.idear.backend.idea.domain.IdeaVersion;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VersionDetailInfo {
	private Long ideaVersionId;
	private Integer versionNumber;
	private String shortDescription;
	private String description;
	private LocalDateTime requestedAt;
	private List<IdeaFileInfo> files;
	private List<IdeaImageInfo> images;

	public static VersionDetailInfo of(IdeaVersion version) {
		List<IdeaFileInfo> fileInfos = version.getFiles().stream()
				.map(IdeaFileInfo::of)
				.collect(Collectors.toList());

		List<IdeaImageInfo> imageInfos = version.getImages().stream()
				.map(IdeaImageInfo::of)
				.collect(Collectors.toList());

		return VersionDetailInfo.builder()
				.ideaVersionId(version.getIdeaVersionId())
				.versionNumber(version.getVersionNumber())
				.shortDescription(version.getShortDescription())
				.description(version.getDescription())
				.requestedAt(version.getRequestedAt())
				.files(fileInfos)
				.images(imageInfos)
				.build();
	}
}
