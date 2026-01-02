package com.idear.backend.idea.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TagAddRequest {
    @NotBlank(message = "태그는 필수입니다")
    @Size(max = 50, message = "태그는 최대 50자까지 가능합니다")
    private String tag;
}
