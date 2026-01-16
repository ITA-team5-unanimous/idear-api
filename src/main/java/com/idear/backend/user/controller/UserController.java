package com.idear.backend.user.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.application.service.UserService;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.dto.request.RegisterPublicKeyRequest;
import com.idear.backend.user.dto.request.SendEmailVerificationRequest;
import com.idear.backend.user.dto.request.UpdateEmailRequest;
import com.idear.backend.user.dto.request.UpdateNameRequest;
import com.idear.backend.user.dto.request.VerifyEmailCodeRequest;
import com.idear.backend.user.dto.response.ProfileImageResponse;
import com.idear.backend.user.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
	public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(
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
	public ResponseEntity<ApiResponse<Void>> updateName(
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
	public ResponseEntity<ApiResponse<Void>> registerPublicKey(
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
	public ResponseEntity<ApiResponse<Void>> deleteUser(
		@Parameter(hidden = true) @ValidatedUser User user
	) {
		userService.deleteUser(user);
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(summary = "이메일 인증 코드 전송", description = "이메일 변경을 위한 인증 코드를 전송합니다.")
	@PostMapping("/email/verification")
	public ResponseEntity<ApiResponse<Void>> sendEmailVerificationCode(
			@Valid @RequestBody SendEmailVerificationRequest request) {
		userService.sendEmailVerificationCode(request.getEmail());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(summary = "이메일 인증 코드 검증", description = "전송된 인증 코드를 검증합니다.")
	@PostMapping("/email/verification/verify")
	public ResponseEntity<ApiResponse<Void>> verifyEmailCode(
			@Valid @RequestBody VerifyEmailCodeRequest request) {
		userService.verifyEmailCode(request.getEmail(), request.getCode());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(summary = "이메일 변경", description = "인증된 이메일로 변경합니다.")
	@PatchMapping("/email")
	public ResponseEntity<ApiResponse<Void>> updateEmail(
			@Parameter(hidden = true) @ValidatedUser User user,
			@Valid @RequestBody UpdateEmailRequest request) {
		userService.updateEmail(user, request.getEmail());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다.")
	@PostMapping(value = "/profile-image", consumes = "multipart/form-data")
	public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
			@Parameter(hidden = true) @ValidatedUser User user,
			@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
		String profileImageUrl = userService.uploadProfileImage(user, file);
		return ResponseEntity.ok(ApiResponse.success(ProfileImageResponse.of(profileImageUrl)));
	}
}
