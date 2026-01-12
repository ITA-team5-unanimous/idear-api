package com.idear.backend.inquiry.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.user.domain.User;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<ApiResponse> createInquiry(
            @Parameter(hidden = true) @ValidatedUser User user,
            @RequestPart("request") InquiryCreateRequest inquiryCreateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        inquiryService.createInquiry(user, inquiryCreateRequest, images);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
