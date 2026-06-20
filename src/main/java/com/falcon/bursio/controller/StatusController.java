package com.falcon.bursio.controller;

import com.falcon.bursio.dto.response.MaintenanceResponse;
import com.falcon.bursio.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class StatusController {

    private final MaintenanceService maintenanceService;

    @GetMapping("/status")
    public ResponseEntity<MaintenanceResponse> getStatus() {
        return ResponseEntity.ok(new MaintenanceResponse(
                maintenanceService.isActive(),
                maintenanceService.getMessage()
        ));
    }
}
