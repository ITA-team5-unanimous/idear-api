package com.idear.backend.inquiry.infrastructure.repository;

import com.idear.backend.inquiry.domain.Inquiry;
import com.idear.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    List<Inquiry> findAllByUserOrderByCreatedAtDesc(User user);
}
