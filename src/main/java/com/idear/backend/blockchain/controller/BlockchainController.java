package com.idear.backend.blockchain.controller;

import com.idear.backend.blockchain.application.BlockchainService;
import com.idear.backend.blockchain.dto.request.RegistrationResultRequest;
import com.idear.backend.global.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Blockchain", description = "블록체인 관련 API")
@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @Hidden
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleRegistration(@RequestBody RegistrationResultRequest request){
        blockchainService.handleRegistrationResult(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
