package com.idear.backend.inquiry.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false)
    private String imageUrl;

    public static InquiryImage createInquiryImage(Inquiry inquiry, String imageUrl) {
        InquiryImage inquiryImage = new InquiryImage();
        inquiryImage.inquiry = inquiry;
        inquiryImage.imageUrl = imageUrl;
        return inquiryImage;
    }
}
