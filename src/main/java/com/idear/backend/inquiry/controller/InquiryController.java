package com.idear.backend.inquiry.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.inquiry.dto.InquiryDetailResponse;
import com.idear.backend.inquiry.dto.InquiryResponse;
import com.idear.backend.inquiry.dto.InquiryUpdateRequest;
import com.idear.backend.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "문의 상세 조회", description = "문의 ID로 특정 문의의 상세 정보를 조회합니다. 발생시각, 브라우저, 기기, 문제상황, 에러화면, 답변 등 모든 정보를 포함합니다.")
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<InquiryDetailResponse>> getInquiryDetail(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "문의 ID", required = true) @PathVariable Long inquiryId) {
        InquiryDetailResponse inquiry = inquiryService.getInquiryDetail(user, inquiryId);
        return ResponseEntity.ok(ApiResponse.success(inquiry));
    }

    @Operation(summary = "문의 수정", description = "문의 내용을 수정합니다. 기기, 브라우저, 문제상황, 이미지를 수정할 수 있습니다. 접수 상태일 때만 수정 가능합니다.")
    @PatchMapping(value = "/{inquiryId}", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse> updateInquiry(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "문의 ID", required = true) @PathVariable Long inquiryId,
            @Parameter(description = "수정할 문의 내용", required = true) @RequestPart("request") InquiryUpdateRequest request,
            @Parameter(description = "에러 이미지 파일 (최대 4장)") @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        inquiryService.updateInquiry(user, inquiryId, request, images);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다. 접수 상태일 때만 삭제 가능합니다.")
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse> deleteInquiry(
            @Parameter(hidden = true) @ValidatedUser User user,
            @Parameter(description = "문의 ID", required = true) @PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(user, inquiryId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
