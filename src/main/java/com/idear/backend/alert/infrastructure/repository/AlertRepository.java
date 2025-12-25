package com.idear.backend.alert.infrastructure.repository;

import com.idear.backend.alert.domain.Alert;
import com.idear.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> getAlertsByUserAndIsReadAndCreatedAtAfter(User user, Boolean isRead, LocalDateTime after);
}
