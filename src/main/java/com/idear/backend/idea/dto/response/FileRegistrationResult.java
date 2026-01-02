package com.idear.backend.idea.dto.response;

import com.idear.backend.idea.domain.IdeaFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileRegistrationResult {
	private Long ideaFileId;
	private String fileName;
	private String fileHash;
	private String salt;
	private String commit;

	public static FileRegistrationResult of(IdeaFile ideaFile) {
		return FileRegistrationResult.builder()
				.ideaFileId(ideaFile.getIdeaFileId())
				.fileName(ideaFile.getOriginalFileName())
				.fileHash(ideaFile.getFileHash())
				.salt(ideaFile.getSalt())
				.commit(ideaFile.getCommit())
				.build();
	}
}
