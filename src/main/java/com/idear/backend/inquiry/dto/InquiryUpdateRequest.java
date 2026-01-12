package com.idear.backend.inquiry.dto;

import java.time.LocalDateTime;

public record InquiryUpdateRequest(
                LocalDateTime occurrenceTime,
                String browser,
                String device,
                String problemDescription) {
}
