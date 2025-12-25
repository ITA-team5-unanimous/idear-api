package com.idear.backend.inquiry.application.service;

import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.inquiry.infrastructure.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public void createInquiry(InquiryCreateRequest request) {
        Inquiry inquiry = Inquiry.createInquiry(request.name(), request.email(), request.title(), request.content());
        inquiryRepository.save(inquiry);
    }
}
