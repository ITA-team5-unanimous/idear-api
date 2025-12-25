package com.idear.backend.global.infrasturcture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//@Disabled("This is an integration test that sends a real email and requires SMTP configuration.")
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendRealEmail() {
        String to = "sohyeoon.jung@gmail.com";
        String subject = "iDear - Test Email";
        String text = "This is a test email from the iDear application.";

        Assertions.assertDoesNotThrow(() -> {
            emailService.sendEmail(to, subject, text);
        });
    }
}
