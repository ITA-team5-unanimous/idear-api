package com.idear.backend.inquiry.dto;

import com.idear.backend.inquiry.domain.InquiryStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryDetailResponse(
        Long id,
        String title,
        LocalDateTime occurrenceTime,
        String browser,
        String device,
        String problemDescription,
        InquiryStatus status,
        List<String> imageUrls,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt) {
}
