package com.idear.backend.user.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.application.service.UserService;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.dto.request.RegisterPublicKeyRequest;
import com.idear.backend.user.dto.request.UpdateNameRequest;
import com.idear.backend.user.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 정보 관리 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(
		summary = "내 정보 조회",
		description = "현재 로그인한 사용자의 정보를 조회합니다."
	)
	@GetMapping
	public ResponseEntity<?> getUserInfo(
		@Parameter(hidden = true) @ValidatedUser User user
	) {
		UserInfoResponse response = userService.getUserInfo(user);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(
		summary = "이름 변경",
		description = "현재 로그인한 사용자의 이름을 변경합니다."
	)
	@PatchMapping("/name")
	public ResponseEntity<?> updateName(
		@Parameter(hidden = true) @ValidatedUser User user,
		@Valid @RequestBody UpdateNameRequest request
	) {
		userService.updateName(user, request.getName());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(
		summary = "퍼블릭 키 등록",
		description = "블록체인 서명용 사용자 퍼블릭 키를 등록합니다."
	)
	@PostMapping("/public-key")
	public ResponseEntity<?> registerPublicKey(
		@Parameter(hidden = true) @ValidatedUser User user,
		@Valid @RequestBody RegisterPublicKeyRequest request
	) {
		userService.registerPublicKey(user, request.getPublicKey());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(
		summary = "회원 탈퇴",
		description = "현재 로그인한 사용자를 회원 탈퇴합니다.(소프트 삭제처리)"
	)
	@DeleteMapping
	public ResponseEntity<?> deleteUser(
		@Parameter(hidden = true) @ValidatedUser User user
	) {
		userService.deleteUser(user);
		return ResponseEntity.ok(ApiResponse.success());
	}
}
