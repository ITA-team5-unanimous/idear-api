package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaFileInfo {
	private Long ideaFileId;
	private String fileName;
	private String filePath;

	public static IdeaFileInfo of(IdeaFile ideaFile) {
		return IdeaFileInfo.builder()
				.ideaFileId(ideaFile.getIdeaFileId())
				.fileName(ideaFile.getOriginalFileName())
				.filePath(ideaFile.getFilePath())
				.build();
	}
}
