package com.idear.backend.user.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.dto.UserInfo;
import com.idear.backend.user.application.service.UserService;
import com.idear.backend.user.dto.request.UpdateNameRequest;
import com.idear.backend.user.dto.response.UserInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<?> getUserInfo(
		@AuthenticationPrincipal UserInfo userInfo
	) {
		UserInfoResponse response = userService.getUserInfo(userInfo.id());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PatchMapping
	public ResponseEntity<?> updateName(
		@AuthenticationPrincipal UserInfo userInfo,
		@Valid @RequestBody UpdateNameRequest request
	) {
		userService.updateName(userInfo.id(), request.getName());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@DeleteMapping
	public ResponseEntity<?> deleteUser(
			@AuthenticationPrincipal UserInfo userInfo
	) {
		userService.deleteUser(userInfo.id());
		return ResponseEntity.ok(ApiResponse.success());
	}
}
