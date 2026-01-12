package com.idear.backend.inquiry.infrastructure.repository;

import com.idear.backend.inquiry.domain.InquiryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryImageRepository extends JpaRepository<InquiryImage, Long> {
}
