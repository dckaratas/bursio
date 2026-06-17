package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.AddEmailDomainRequest;
import com.falcon.bursio.dto.request.AddUniversityRequest;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.*;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.UniversityRepository;
import com.falcon.bursio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final UniversityRepository universityRepository;

    // ── Kullanıcı Yönetimi ───────────────────────────────────────────────────

    public PageResponse<UserSummaryResponse> getAllUsers(int page, int size) {
        return PageResponse.from(
                userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
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

    public PageResponse<ReportDetailResponse> getReports(ReportStatus status, int page, int size) {
        if (status != null) {
            return PageResponse.from(
                    reportRepository.findByReportStatus(
                                    status, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                            .map(ReportDetailResponse::from)
            );
        }
        return PageResponse.from(
                reportRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
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

    // ── Üniversite Yönetimi ──────────────────────────────────────────────────

    public PageResponse<UniversityResponse> getAllUniversities(int page, int size) {
        return PageResponse.from(
                universityRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()))
                        .map(UniversityResponse::from)
        );
    }

    @Transactional
    public UniversityResponse addUniversity(AddUniversityRequest request) {
        University university = University.builder()
                .name(request.name())
                .city(request.city())
                .emailDomains(new ArrayList<>(request.emailDomains()))
                .active(true)
                .build();

        universityRepository.save(university);
        return UniversityResponse.from(university);
    }

    @Transactional
    public UniversityResponse addEmailDomain(Long universityId, AddEmailDomainRequest request) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDENT_PROFILE_NOT_FOUND));

        if (!university.getEmailDomains().contains(request.domain())) {
            university.getEmailDomains().add(request.domain());
            universityRepository.save(university);
        }

        return UniversityResponse.from(university);
    }

    @Transactional
    public MessageResponse toggleUniversityStatus(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDENT_PROFILE_NOT_FOUND));

        university.setActive(!university.isActive());
        universityRepository.save(university);

        return new MessageResponse(university.isActive()
                ? "Üniversite aktif edildi."
                : "Üniversite pasif yapıldı.");
    }
}