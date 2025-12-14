package com.idear.backend.idea.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileHashInfo {
	private Long ideaFileId;
	private String fileName;
	private String fileHash;
	private Long timestamp;
}
