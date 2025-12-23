package com.idear.backend.auth.controller;

import com.idear.backend.auth.application.service.AuthService;
import com.idear.backend.auth.dto.request.RefreshTokenRequest;
import com.idear.backend.auth.dto.response.TokenResponse;
import com.idear.backend.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 및 토큰 관리 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "토큰 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse tokenResponse = authService.reissue(request.getRefresh());
        ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse);
        return ResponseEntity.ok(response);
    }
}
