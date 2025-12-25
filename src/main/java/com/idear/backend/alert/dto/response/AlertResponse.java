package com.idear.backend.alert.dto.response;

import com.idear.backend.alert.domain.Alert;
import com.idear.backend.alert.domain.AlertType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertResponse {
    private Long alertId;
    private AlertType alertType;
    private String content;
    private Long ideaId;
    private Long ideaFileId;
    private LocalDateTime createdAt;

    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .alertId(alert.getAlertId())
                .alertType(alert.getAlertType())
                .content(alert.getContent())
                .ideaId(alert.getIdeaId())
                .ideaFileId(alert.getIdeaFileId())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
