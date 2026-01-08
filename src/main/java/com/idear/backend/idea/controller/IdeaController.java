package com.idear.backend.idea.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.idea.application.IdeaService;
import com.idear.backend.idea.dto.request.FileSignatureRequest;
import com.idear.backend.idea.dto.request.IdeaCreateRequest;
import com.idear.backend.idea.dto.request.IdeaUpdateRequest;
import com.idear.backend.idea.dto.response.*;
import com.idear.backend.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Idea", description = "아이디어 등록 및 관리 API")
@RestController
@RequestMapping("/ideas")
@RequiredArgsConstructor
public class IdeaController {

	private final IdeaService ideaService;

	@Operation(
		summary = "아이디어 초기 등록",
		description = "아이디어 및 파일 초기 등록 요청. 파일은 S3에 업로드되고 각 파일의 해시값과 타임스탬프를 반환합니다. (등록 1단계)"
	)
	@PostMapping(consumes = {"multipart/form-data"})
	public ResponseEntity<ApiResponse<IdeaRegistrationInitResponse>> initIdeaRegistration(
		@Parameter(hidden = true) @ValidatedUser User user,
		@Parameter(description = "아이디어 등록 정보", required = true)
		@Valid @RequestPart(value = "ideaData") IdeaCreateRequest ideaCreateRequest,
		@Parameter(description = "아이디어 설명용 이미지 파일 목록")
		@RequestPart(value = "images", required = false) List<MultipartFile> images,
		@Parameter(description = "블록체인 등록 대상 파일 목록 (최대 20MB)")
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) throws IOException {
		IdeaRegistrationInitResponse response = ideaService.initIdeaRegistration(user, ideaCreateRequest, images, files);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "파일 서명 제출 및 블록체인 등록",
		description = "클라이언트에서 생성한 파일별 서명을 제출하고, 블록체인에 commit을 등록합니다. (등록 2단계)"
	)
	@PostMapping("/{ideaId}/signatures")
	public ResponseEntity<ApiResponse<IdeaRegistrationCompleteResponse>> submitSignatures(
		@Parameter(hidden = true) @ValidatedUser User user,
		@Parameter(description = "아이디어 ID", required = true, example = "1")
		@PathVariable("ideaId") Long ideaId,
		@Valid @RequestBody FileSignatureRequest request
	) {
		IdeaRegistrationCompleteResponse response = ideaService.submitSignatures(user, ideaId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
			summary = "아이디어 수정",
			description = "아이디어의 파일 및 메타데이터를 수정합니다. 파일 변경 시 새 버전 생성, 메타만 변경 시 최신 버전 수정."
	)
	@PatchMapping(value = "/{ideaId}", consumes = {"multipart/form-data"})
	public ResponseEntity<ApiResponse<IdeaUpdateResponse>> updateIdea(
			@Parameter(hidden = true) @ValidatedUser User user,
			@Parameter(description = "아이디어 ID", required = true, example = "1")
			@PathVariable("ideaId") Long ideaId,
			@Parameter(description = "수정 정보 (삭제 대상 파일/이미지 ID, 메타데이터)")
			@RequestPart(value = "ideaData", required = false) IdeaUpdateRequest ideaUpdateRequest,
			@Parameter(description = "추가할 이미지 목록")
			@RequestPart(value = "images", required = false) List<MultipartFile> images,
			@Parameter(description = "추가할 파일 목록")
			@RequestPart(value = "files", required = false) List<MultipartFile> files
	) throws IOException {
		IdeaUpdateResponse response = ideaService.updateIdea(user, ideaId, ideaUpdateRequest, images, files);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "내 아이디어 목록 조회",
		description = "현재 로그인한 사용자가 등록한 모든 아이디어 목록을 조회합니다."
	)
	@GetMapping
	public ResponseEntity<ApiResponse<List<IdeaSummaryResponse>>> getMyIdeas(
		@Parameter(hidden = true) @ValidatedUser User user
	) {
		List<IdeaSummaryResponse> ideas = ideaService.getIdeasByUser(user);
		return ResponseEntity.ok(ApiResponse.success(ideas));
	}

	@Operation(
		summary = "아이디어 상세 조회",
		description = "특정 아이디어의 모든 버전에 대해 최신순으로 조회합니다."
	)
	@GetMapping("/{ideaId}")
	public ResponseEntity<ApiResponse<IdeaWithVersionsResponse>> getIdea(
			@Parameter(hidden = true) @ValidatedUser User user,
			@Parameter(description = "조회할 아이디어 ID", required = true, example = "1")
			@PathVariable("ideaId") Long ideaId
	) {
		IdeaWithVersionsResponse response = ideaService.getIdeaVersions(user, ideaId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "아이디어 삭제",
		description = "특정 아이디어를 소프트 삭제합니다. 블록체인에 등록된 데이터는 삭제되지 않습니다."
	)
	@DeleteMapping("/{ideaId}")
	public ResponseEntity<ApiResponse<Void>> deleteIdea(
		@Parameter(hidden = true) @ValidatedUser User user,
		@Parameter(description = "삭제할 아이디어 ID", required = true, example = "1")
		@PathVariable("ideaId") Long ideaId
	) {
		ideaService.deleteIdea(user, ideaId);
		return ResponseEntity.ok(ApiResponse.success());
	}
}
