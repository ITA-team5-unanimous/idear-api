package com.idear.backend.idea.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignatureData {
	@NotNull
	private Long ideaFileId;
	@NotNull
	private String userSignature;
}
