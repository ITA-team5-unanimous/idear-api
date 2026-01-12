package com.idear.backend.inquiry.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.inquiry.dto.InquiryResponse;
import com.idear.backend.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의 생성", description = "사용자 문의를 생성합니다. 문의 내용과 함께 최대 4장의 에러 이미지를 첨부할 수 있습니다.")
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse> createInquiry(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "문의 내용", required = true) @RequestPart("request") InquiryCreateRequest inquiryCreateRequest,
            @Parameter(description = "에러 이미지 파일 (최대 4장)") @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        inquiryService.createInquiry(user, inquiryCreateRequest, images);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "사용자 문의 내역 조회", description = "로그인한 사용자의 모든 문의 내역을 조회합니다. 최신순으로 정렬됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getUserInquiries(
            @Parameter(hidden = true) @ValidatedUser User user) {
        List<InquiryResponse> inquiries = inquiryService.getUserInquiries(user);
        return ResponseEntity.ok(ApiResponse.success(inquiries));
    }
}
