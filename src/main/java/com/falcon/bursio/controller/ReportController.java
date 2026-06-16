package com.falcon.bursio.controller;


import com.falcon.bursio.dto.request.ReportRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.security.SecurityUtils;
import com.falcon.bursio.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<MessageResponse> createReport(
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(SecurityUtils.getCurrentUser(), request));
    }
}