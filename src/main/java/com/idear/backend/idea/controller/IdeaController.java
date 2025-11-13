package com.idear.backend.idea.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.idea.application.IdeaService;
import com.idear.backend.idea.dto.request.IdeaRegisterRequest;
import com.idear.backend.idea.dto.response.IdeaResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/idea")
@RequiredArgsConstructor
public class IdeaController {

	private final IdeaService ideaService;

	/**
	 * POST /api/idea : 아이디어 등록
	 */
	@PostMapping(consumes = {"multipart/form-data"})
	public ResponseEntity<ApiResponse<Void>> registerIdea(
		@Valid @RequestPart(value = "request") IdeaRegisterRequest ideaRegisterRequest,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) throws IOException {
		ideaService.registerIdea(ideaRegisterRequest, files);
		return ResponseEntity.ok(ApiResponse.success());
	}

	/**
	 * DELETE /api/idea/{ideaId} : 아이디어 삭제
	 */
	@DeleteMapping("/{ideaId}")
	public ResponseEntity<ApiResponse<Void>> deleteIdea(
		@PathVariable("ideaId") Long ideaId
	) {
		ideaService.deleteIdea(ideaId);
		return ResponseEntity.ok(ApiResponse.success());
	}

	/**
	 * GET /api/idea/{userId} : 유저별 아이디어 조회
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<List<IdeaResponse>>> getIdeasByUser(
		@PathVariable("userId") Long userId
	) {
		List<IdeaResponse> ideas = ideaService.getIdeasByUser(userId);
		return ResponseEntity.ok(ApiResponse.success(ideas));
	}
}
