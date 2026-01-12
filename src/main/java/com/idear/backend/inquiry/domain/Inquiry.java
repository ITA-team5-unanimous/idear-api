package com.idear.backend.inquiry.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.idear.backend.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDateTime occurrenceTime;

    @Column(nullable = false)
    private String browser;

    @Column(nullable = false)
    private String device;

    @Lob
    @Column(nullable = false)
    private String problemDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryImage> inquiryImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Lob
    @Column
    private String answer;

    @Column
    private LocalDateTime answeredAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private Inquiry(String title, LocalDateTime occurrenceTime, String browser, String device,
            String problemDescription, User user) {
        this.title = title;
        this.occurrenceTime = occurrenceTime;
        this.browser = browser;
        this.device = device;
        this.problemDescription = problemDescription;
        this.status = InquiryStatus.RECEIVED;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public static Inquiry createInquiry(String title, LocalDateTime occurrenceTime, String browser, String device,
            String problemDescription, User user) {
        return new Inquiry(title, occurrenceTime, browser, device, problemDescription, user);
    }

    public void addInquiryImage(InquiryImage inquiryImage) {
        this.inquiryImages.add(inquiryImage);
    }

    public void removeInquiryImage(InquiryImage inquiryImage) {
        this.inquiryImages.remove(inquiryImage);
    }

    public void answerToInquiry(String answerContent) {
        this.answer = answerContent;
        this.answeredAt = LocalDateTime.now();
        this.status = InquiryStatus.ANSWERED;
    }
}
