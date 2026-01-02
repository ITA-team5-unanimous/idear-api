package com.idear.backend.idea.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.idea.application.IdeaVersionService;
import com.idear.backend.idea.dto.request.TagAddRequest;
import com.idear.backend.idea.dto.response.TagResponse;
import com.idear.backend.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "IdeaVersion", description = "아이디어 버전 관리 API")
@RestController
@RequestMapping("/idea-versions")
@RequiredArgsConstructor
public class IdeaVersionController {

    private final IdeaVersionService ideaVersionService;

    @Operation(
        summary = "태그 추가",
        description = "특정 아이디어 버전에 태그를 추가합니다."
    )
    @PostMapping("/{versionId}/tags")
    public ResponseEntity<ApiResponse<TagResponse>> addTag(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "아이디어 버전 ID", required = true, example = "1")
            @PathVariable("versionId") Long versionId,
            @Valid @RequestBody TagAddRequest request
    ) {
        TagResponse response = ideaVersionService.addTag(user, versionId, request.getTag());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "태그 삭제",
        description = "특정 아이디어 버전의 태그를 삭제합니다."
    )
    @DeleteMapping("/{versionId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "아이디어 버전 ID", required = true, example = "1")
            @PathVariable("versionId") Long versionId,
            @Parameter(description = "삭제할 태그 ID", required = true, example = "1")
            @PathVariable("tagId") Long tagId
    ) {
        ideaVersionService.deleteTag(user, versionId, tagId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
