package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdeaFileInfo {
	private String fileName;
	private String filePath;
	private IdeaFile.RegisterStatus status;
	private String txHash;

	public static IdeaFileInfo of(IdeaFile ideaFile) {
		return IdeaFileInfo.builder()
				.fileName(ideaFile.getOriginalFileName())
				.filePath(ideaFile.getFilePath())
				.status(ideaFile.getRegisterStatus())
				.txHash(ideaFile.getTxHash())
				.build();
	}
}
