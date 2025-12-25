package com.idear.backend.inquiry.controller;

import com.idear.backend.global.ApiResponse;
import com.idear.backend.inquiry.application.service.InquiryService;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse> createInquiry(@RequestBody InquiryCreateRequest request) {
        inquiryService.createInquiry(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
