package com.idear.backend.inquiry.dto;

import com.idear.backend.inquiry.domain.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryResponse(
        Long id,
        String category,
        String problemDescription,
        InquiryStatus status,
        LocalDateTime createdAt) {
}
