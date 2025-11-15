package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaFileResponse {
	private Long fileId;
	private String originalFileName;
	private String fileName;
	private String fileType;
	private String filePath;

	public static IdeaFileResponse of(IdeaFile ideaFile) {
		return IdeaFileResponse.builder()
			.fileId(ideaFile.getFileId())
			.originalFileName(ideaFile.getOriginalFileName())
			.fileName(ideaFile.getFileName())
			.fileType(String.valueOf(ideaFile.getFileType()))
			.filePath(ideaFile.getFilePath())
			.build();
	}
}
