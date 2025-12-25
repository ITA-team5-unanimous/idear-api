package com.idear.backend.alert.application.service;

import com.idear.backend.alert.domain.Alert;
import com.idear.backend.alert.dto.response.AlertResponse;
import com.idear.backend.alert.infrastructure.repository.AlertRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<AlertResponse> getUnreadAlerts(User user){
        LocalDateTime alertsAfter = LocalDateTime.now().minusWeeks(4);
        List<Alert> unreadAlerts = alertRepository.getAlertsByUserAndIsReadAndCreatedAtAfter(user, false, alertsAfter);

        return unreadAlerts.stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Transactional
    public void readAlerts(User user, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> CustomException.of(ErrorCode.ALERT_NOT_FOUND));

        if (!alert.getUser().getUserId().equals(user.getUserId())) {
            throw CustomException.of(ErrorCode.USER_NOT_OWNER);
        }
        alert.read();
    }

    @Transactional
    public void createRegistrationAlert(String content, IdeaFile ideaFile) {
        Alert alert = Alert.ofRegistration(content, ideaFile);
        alertRepository.save(alert);
    }
}
