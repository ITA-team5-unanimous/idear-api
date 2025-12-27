package com.idear.backend.alert.controller;

import com.idear.backend.alert.application.service.AlertService;
import com.idear.backend.alert.dto.response.AlertResponse;
import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.domain.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alert", description = "알림 관련 API")
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnreadAlerts(
    	@Parameter(hidden = true) @ValidatedUser User user
    ){
        List<AlertResponse> response = alertService.getUnreadAlerts(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> readAlert(
        @Parameter(hidden = true) @ValidatedUser User user,
        @Parameter(description = "알림 ID", required = true, example = "1")
        @PathVariable Long alertId
    ){
        alertService.readAlerts(user, alertId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
