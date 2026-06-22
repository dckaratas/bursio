package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.dto.response.ReportDetailResponse;
import com.falcon.bursio.dto.response.UserSummaryResponse;
import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.ReportReason;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ReportRepository reportRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Report report;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("donor@gmail.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .role(Role.DONOR)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        report = Report.builder()
                .reporter(user)
                .reporterEmail("donor@gmail.com")
                .reportedUserEmail("student@ogr.gazi.edu.tr")
                .reason(ReportReason.SPAM)
                .description("Spam yapıyor.")
                .reportStatus(ReportStatus.OPEN)
                .build();
    }

    // ── getAllUsers ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Kullanıcılar başarıyla listelenebilmeli")
    void getAllUsers_success() {
        when(userRepository.searchUsers(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        PageResponse<UserSummaryResponse> result = adminService.getAllUsers(null, null, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).email()).isEqualTo("donor@gmail.com");
        verify(userRepository).searchUsers(any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Role'e göre kullanıcı filtrelenebilmeli")
    void getUsersByRole_success() {
        when(userRepository.findByRole(eq(Role.DONOR), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        PageResponse<UserSummaryResponse> result = adminService.getUsersByRole(Role.DONOR, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).role()).isEqualTo(Role.DONOR);
    }

    // ── updateUserStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Kullanıcı durumu başarıyla güncellenebilmeli")
    void updateUserStatus_success() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(AccountStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSummaryResponse result = adminService.updateUserStatus(1L, request);

        assertThat(result.status()).isEqualTo(AccountStatus.SUSPENDED);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Olmayan kullanıcının durumu güncellenememeli")
    void updateUserStatus_user_not_found_throws_exception() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(AccountStatus.SUSPENDED);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUserStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.USER_NOT_FOUND);

        verify(userRepository, never()).save(any());
    }

    // ── getReports ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Raporlar başarıyla listelenebilmeli")
    void getReports_success() {
        when(reportRepository.searchReports(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(report)));

        PageResponse<ReportDetailResponse> result = adminService.getReports(null, null, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).reporterEmail()).isEqualTo("donor@gmail.com");
        verify(reportRepository).searchReports(any(), any(), any(Pageable.class));
    }

    // ── updateReportStatus ───────────────────────────────────────────────────

    @Test
    @DisplayName("Rapor durumu başarıyla güncellenebilmeli")
    void updateReportStatus_success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportDetailResponse result = adminService.updateReportStatus(1L, ReportStatus.RESOLVED);

        assertThat(result.reportStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(reportRepository).save(report);
    }

    @Test
    @DisplayName("Olmayan raporun durumu güncellenememeli")
    void updateReportStatus_report_not_found_throws_exception() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateReportStatus(99L, ReportStatus.RESOLVED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.REPORT_NOT_FOUND);

        verify(reportRepository, never()).save(any());
    }
}
