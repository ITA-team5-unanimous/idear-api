package com.idear.backend.idea.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.idea.application.IdeaService;
import com.idear.backend.idea.dto.request.FileSignatureRequest;
import com.idear.backend.idea.dto.request.IdeaCreateRequest;
import com.idear.backend.idea.dto.response.IdeaDetailResponse;
import com.idear.backend.idea.dto.response.IdeaRegistrationCompleteResponse;
import com.idear.backend.idea.dto.response.IdeaRegistrationInitResponse;
import com.idear.backend.idea.dto.response.IdeaSummaryResponse;
import com.idear.backend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ideas")
@RequiredArgsConstructor
public class IdeaController {

	private final IdeaService ideaService;

	/**
	 * POST /api/ideas : 아이디어 및 파일 초기 등록 요청, fileHash랑 timestamp 반환 (등록 1단계)
	 */
	@PostMapping(consumes = {"multipart/form-data"})
	public ResponseEntity<ApiResponse<IdeaRegistrationInitResponse>> initIdeaRegistration(
		@ValidatedUser User user,
		@Valid @RequestPart(value = "ideaData") IdeaCreateRequest ideaCreateRequest,
		@RequestPart(value = "images", required = false) List<MultipartFile> images,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) throws IOException {
		IdeaRegistrationInitResponse response = ideaService.initIdeaRegistration(user, ideaCreateRequest, images, files);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * POST /api/ideas/{ideaId}/signatures : 유저 서명 제출 및 블록체인 등록 (등록 2단계)
	 */
	@PostMapping("/{ideaId}/signatures")
	public ResponseEntity<ApiResponse<IdeaRegistrationCompleteResponse>> submitSignatures(
		@ValidatedUser User user,
		@PathVariable("ideaId") Long ideaId,
		@Valid @RequestBody FileSignatureRequest request
	) {
		IdeaRegistrationCompleteResponse response = ideaService.submitSignatures(user, ideaId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * GET /api/ideas : 내 아이디어 목록 조회
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<IdeaSummaryResponse>>> getMyIdeas(
		@ValidatedUser User user
	) {
		List<IdeaSummaryResponse> ideas = ideaService.getIdeasByUser(user);
		return ResponseEntity.ok(ApiResponse.success(ideas));
	}

	/**
	 * GET /api/ideas/{ideaId} : 아이디어 상세 조회
	 */
	@GetMapping("/{ideaId}")
	public ResponseEntity<ApiResponse<IdeaDetailResponse>> getIdeaDetail(
			@ValidatedUser User user,
			@PathVariable("ideaId") Long ideaId
	) {
		IdeaDetailResponse response = ideaService.getIdeaDetail(user, ideaId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * DELETE /api/ideas/{ideaId} : 아이디어 삭제
	 */
	@DeleteMapping("/{ideaId}")
	public ResponseEntity<ApiResponse<Void>> deleteIdea(
		@ValidatedUser User user,
		@PathVariable("ideaId") Long ideaId
	) {
		ideaService.deleteIdea(user, ideaId);
		return ResponseEntity.ok(ApiResponse.success());
	}
}
