package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaImage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaImageInfo {
	private Long ideaImageId;
	private String fileName;
	private String filePath;

	public static IdeaImageInfo of(IdeaImage ideaImage) {
		return IdeaImageInfo.builder()
				.ideaImageId(ideaImage.getIdeaImageId())
				.fileName(ideaImage.getOriginalFileName())
				.filePath(ideaImage.getFilePath())
				.build();
	}
}
