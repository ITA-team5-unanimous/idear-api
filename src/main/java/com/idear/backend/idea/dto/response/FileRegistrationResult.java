package com.idear.backend.idea.dto.response;

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
}
