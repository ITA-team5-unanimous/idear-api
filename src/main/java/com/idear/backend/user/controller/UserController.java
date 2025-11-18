package com.idear.backend.user.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.application.service.UserService;
import com.idear.backend.user.domain.User;
import com.idear.backend.user.dto.request.UpdateNameRequest;
import com.idear.backend.user.dto.response.UserInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<?> getUserInfo(
		@ValidatedUser User user
	) {
		UserInfoResponse response = userService.getUserInfo(user);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PatchMapping
	public ResponseEntity<?> updateName(
		@ValidatedUser User user,
		@Valid @RequestBody UpdateNameRequest request
	) {
		userService.updateName(user, request.getName());
		return ResponseEntity.ok(ApiResponse.success());
	}

	@DeleteMapping
	public ResponseEntity<?> deleteUser(
		@ValidatedUser User user
	) {
		userService.deleteUser(user);
		return ResponseEntity.ok(ApiResponse.success());
	}
}
