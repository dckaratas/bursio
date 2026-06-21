package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.*;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    // ── Kullanıcı Yönetimi ───────────────────────────────────────────────────

    public PageResponse<UserSummaryResponse> getAllUsers(String query, Role role, int page, int size) {
        return PageResponse.from(
                userRepository.searchUsers(
                                query,
                                role,
                                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                        .map(UserSummaryResponse::from)
        );
    }

    public PageResponse<UserSummaryResponse> getUsersByRole(Role role, int page, int size) {
        return PageResponse.from(
                userRepository.findByRole(role, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                        .map(UserSummaryResponse::from)
        );
    }

    @Transactional
    public UserSummaryResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setStatus(request.status());
        userRepository.save(user);
        return UserSummaryResponse.from(user);
    }

    // ── Rapor Yönetimi ───────────────────────────────────────────────────────

    public PageResponse<ReportDetailResponse> getReports(String query, ReportStatus status, int page, int size) {
        return PageResponse.from(
                reportRepository.searchReports(
                                query,
                                status,
                                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                        .map(ReportDetailResponse::from)
        );
    }

    @Transactional
    public ReportDetailResponse updateReportStatus(Long reportId, ReportStatus status) {
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.REPORT_NOT_FOUND));

        report.setReportStatus(status);
        reportRepository.save(report);
        return ReportDetailResponse.from(report);
    }

}