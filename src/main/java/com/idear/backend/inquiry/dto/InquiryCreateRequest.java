package com.idear.backend.inquiry.dto;

import java.time.LocalDateTime;

public record InquiryCreateRequest(
		String category,
        LocalDateTime occurrenceTime,
        String browser,
        String device,
        String problemDescription
){}

