package com.idear.backend.email.controller;

import com.idear.backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/blockchain")
    public ResponseEntity<String> sendBlockchainEmail(@RequestBody TestEmailRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", request.userName());
        variables.put("registrationStatus", "SUCCESS");
        variables.put("failureReason", null);

        emailService.sendEmailWithTemplate(
                request.email(),
                "iDear - 블록체인 등록 결과 테스트",
                "blockchain-registration.html",
                variables
        );
        return ResponseEntity.ok("Blockchain registration test email sent.");
    }

    @PostMapping("/inquiry")
    public ResponseEntity<String> sendInquiryEmail(@RequestBody TestEmailRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", request.userName());
        variables.put("inquiryContent", "이것은 문의 내용 테스트입니다.");
        variables.put("responseContent", "이것은 답변 내용 테스트입니다.");

        emailService.sendEmailWithTemplate(
                request.email(),
                "iDear - 문의 답변 테스트",
                "inquiry-response.html",
                variables
        );
        return ResponseEntity.ok("Inquiry response test email sent.");
    }

    public record TestEmailRequest(String email, String userName) {}
}
