package com.idear.backend.inquiry.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inquirerName;

    @Column(nullable = false)
    private String inquirerEmail;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public static Inquiry createInquiry(String name, String email, String title, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.inquirerName = name;
        inquiry.inquirerEmail = email;
        inquiry.title = title;
        inquiry.content = content;
        inquiry.status = InquiryStatus.PENDING;
        return inquiry;
    }

    public void markAsAnswered() {
        this.status = InquiryStatus.ANSWERED;
    }
}
