package com.falcon.bursio.controller;

import com.falcon.bursio.dto.request.AddEmailDomainRequest;
import com.falcon.bursio.dto.request.AddUniversityRequest;
import com.falcon.bursio.dto.request.MaintenanceRequest;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.*;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.service.AdminService;
import com.falcon.bursio.service.MaintenanceService;
import com.falcon.bursio.service.UniversityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UniversityService universityService;
    private final MaintenanceService maintenanceService;

    // ── Kullanıcı Yönetimi ───────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserSummaryResponse>> getAllUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(query, role, page, size));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserDetail(userId));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserSummaryResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, request));
    }

    // ── Rapor Yönetimi ───────────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportDetailResponse>> getReports(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getReports(query, status, page, size));
    }

    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<ReportDetailResponse> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status) {
        return ResponseEntity.ok(adminService.updateReportStatus(reportId, status));
    }

    // ── Üniversite Yönetimi ──────────────────────────────────────────────────

    @GetMapping("/universities")
    public ResponseEntity<PageResponse<UniversityResponse>> getAllUniversities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(universityService.getUniversities(query, active, page, size));
    }

    @PostMapping("/universities")
    public ResponseEntity<UniversityResponse> addUniversity(
            @Valid @RequestBody AddUniversityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(universityService.addUniversity(request));
    }

    @PostMapping("/universities/{universityId}/domains")
    public ResponseEntity<UniversityResponse> addEmailDomain(
            @PathVariable Long universityId,
            @Valid @RequestBody AddEmailDomainRequest request) {
        return ResponseEntity.ok(universityService.addEmailDomain(universityId, request));
    }

    @PatchMapping("/universities/{universityId}/toggle")
    public ResponseEntity<MessageResponse> toggleUniversityStatus(
            @PathVariable Long universityId) {
        return ResponseEntity.ok(universityService.toggleUniversityStatus(universityId));
    }

    // ── Bakım Modu ───────────────────────────────────────────────────────────

    @GetMapping("/maintenance")
    public ResponseEntity<MaintenanceResponse> getMaintenanceStatus() {
        return ResponseEntity.ok(new MaintenanceResponse(
                maintenanceService.isActive(),
                maintenanceService.getMessage()
        ));
    }

    @PutMapping("/maintenance")
    public ResponseEntity<MaintenanceResponse> setMaintenance(
            @RequestBody MaintenanceRequest request) {
        maintenanceService.setMaintenance(request.active(), request.message());
        return ResponseEntity.ok(new MaintenanceResponse(
                maintenanceService.isActive(),
                maintenanceService.getMessage()
        ));
    }
}