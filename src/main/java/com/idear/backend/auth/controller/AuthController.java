package com.idear.backend.auth.controller;

import com.idear.backend.auth.application.service.AuthService;
import com.idear.backend.auth.dto.request.RefreshTokenRequest;
import com.idear.backend.auth.dto.response.TokenResponse;
import com.idear.backend.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.reissue(request.getRefresh());
        ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse);
        return ResponseEntity.ok(response);
    }
}
