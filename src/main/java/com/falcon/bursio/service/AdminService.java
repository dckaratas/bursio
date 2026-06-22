package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.*;
import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.StudentProfileRepository;
import com.falcon.bursio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final StudentProfileRepository studentProfileRepository;

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

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        StudentProfileResponse profile = null;
        if (user.getRole() == Role.STUDENT) {
            profile = studentProfileRepository.findByUserId(userId)
                    .map(sp -> StudentProfileResponse.from(sp, true))  // admin her şeyi görebilir
                    .orElse(null);
        }

        Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
        List<MatchRequest> matches = user.getRole() == Role.STUDENT
                ? matchRequestRepository.findByStudentIdOrderByCreatedAtDesc(userId, pageable).getContent()
                : matchRequestRepository.findByDonorIdOrderByCreatedAtDesc(userId, pageable).getContent();

        return AdminUserDetailResponse.from(user, profile, matches);
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

    @Transactional(readOnly = true)
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