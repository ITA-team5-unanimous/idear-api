package com.idear.backend.inquiry.application.service;

import com.idear.backend.email.service.EmailService;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.inquiry.dto.InquiryCreateRequest;
import com.idear.backend.inquiry.infrastructure.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final EmailService emailService;

    @Transactional
    public void createInquiry(InquiryCreateRequest request) {
        Inquiry inquiry = Inquiry.createInquiry(request.name(), request.email(), request.title(), request.content());
        inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Inquiry findInquiryById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND_INQUIRY));
    }

    @Transactional
    public void replyToInquiry(Long id, String responseContent) {
        Inquiry inquiry = findInquiryById(id);
        if (inquiry.getStatus() == com.idear.backend.inquiry.domain.InquiryStatus.ANSWERED) {
            CustomException.of(ErrorCode.ALREADY_ANSWERED);
        }

        // Send email
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", inquiry.getInquirerName());
        variables.put("inquiryContent", inquiry.getContent());
        variables.put("responseContent", responseContent);

        emailService.sendEmailWithTemplate(
                inquiry.getInquirerEmail(),
                "[iDear] 문의주신 내용에 대한 답변입니다.",
                "inquiry-response.html",
                variables
        );

        inquiry.markAsAnswered();
        inquiryRepository.save(inquiry);
    }
}
