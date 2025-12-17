package com.idear.backend.idea.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class FileSignatureRequest {
	@NotEmpty
	@Valid
	private List<SignatureData> signatures;
}
