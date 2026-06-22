package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.UpdateUserStatusRequest;
import com.falcon.bursio.dto.response.AdminUserDetailResponse;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.dto.response.ReportDetailResponse;
import com.falcon.bursio.dto.response.UserSummaryResponse;
import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.entity.enums.ReportReason;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.StudentProfileRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private MatchRequestRepository matchRequestRepository;
    @Mock private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private AdminService adminService;

    private User donor;
    private User student;
    private User user;     // donor alias — mevcut testler bozulmasın
    private Report report;

    @BeforeEach
    void setUp() {
        donor = User.builder()
                .email("donor@gmail.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .role(Role.DONOR)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        student = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .firstName("Ayşe")
                .lastName("Kaya")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        user = donor;  // mevcut testler için alias

        report = Report.builder()
                .reporter(donor)
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
        assertThat(result.content().getFirst().email()).isEqualTo("donor@gmail.com");
        verify(userRepository).searchUsers(any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Role'e göre kullanıcı filtrelenebilmeli")
    void getUsersByRole_success() {
        when(userRepository.findByRole(eq(Role.DONOR), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        PageResponse<UserSummaryResponse> result = adminService.getUsersByRole(Role.DONOR, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().role()).isEqualTo(Role.DONOR);
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
        assertThat(result.content().getFirst().reporterEmail()).isEqualTo("donor@gmail.com");
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

    // ── getUserDetail ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Öğrenci kullanıcı için profil ve eşleşmeler dönmeli")
    void getUserDetail_student_returns_profile_and_matches() {
        University uni = University.builder().name("Gazi Üniversitesi").city("Ankara").build();
        StudentProfile profile = StudentProfile.builder()
                .user(student)
                .university(uni)
                .department("Bilgisayar Mühendisliği")
                .grade(3)
                .gpa(new BigDecimal("3.20"))
                .motivation("Burs almak istiyorum.")
                .contactPreference(ContactPreference.EMAIL)
                .contactValue("test@ogr.gazi.edu.tr")
                .profileComplete(true)
                .build();

        MatchRequest match = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(studentProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(matchRequestRepository.findByStudentIdOrderByCreatedAtDesc(eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match)));

        AdminUserDetailResponse result = adminService.getUserDetail(2L);

        assertThat(result.role()).isEqualTo(Role.STUDENT);
        assertThat(result.studentProfile()).isNotNull();
        assertThat(result.studentProfile().department()).isEqualTo("Bilgisayar Mühendisliği");
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().getFirst().status()).isEqualTo(MatchStatus.PENDING);
        // öğrenci perspektifinden → counterpart donor'dır
        assertThat(result.matches().getFirst().counterpartEmail()).isEqualTo("donor@gmail.com");

        verify(studentProfileRepository).findByUserId(2L);
        verify(matchRequestRepository).findByStudentIdOrderByCreatedAtDesc(eq(2L), any(Pageable.class));
        verify(matchRequestRepository, never()).findByDonorIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("Donör kullanıcı için profil null, eşleşmeler dönmeli")
    void getUserDetail_donor_returns_no_profile_and_matches() {
        MatchRequest match = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.ACCEPTED)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(donor));
        when(matchRequestRepository.findByDonorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match)));

        AdminUserDetailResponse result = adminService.getUserDetail(1L);

        assertThat(result.role()).isEqualTo(Role.DONOR);
        assertThat(result.studentProfile()).isNull();
        assertThat(result.matches()).hasSize(1);
        // ACCEPTED → öğrenci adı görünür
        assertThat(result.matches().getFirst().counterpartName()).isEqualTo("Ayşe Kaya");
        assertThat(result.matches().getFirst().counterpartEmail()).isEqualTo("test@ogr.gazi.edu.tr");

        verify(studentProfileRepository, never()).findByUserId(any());
        verify(matchRequestRepository).findByDonorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("PENDING eşleşmede donör için öğrenci kimliği gizlenmeli")
    void getUserDetail_donor_pending_match_hides_student_identity() {
        MatchRequest match = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(donor));
        when(matchRequestRepository.findByDonorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match)));

        AdminUserDetailResponse result = adminService.getUserDetail(1L);

        assertThat(result.matches().getFirst().counterpartName()).isEqualTo("Kimlik Gizli");
        assertThat(result.matches().getFirst().counterpartEmail()).isEqualTo("—");
    }

    @Test
    @DisplayName("Profili olmayan öğrenci için studentProfile null dönmeli")
    void getUserDetail_student_without_profile_returns_null_profile() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(studentProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(matchRequestRepository.findByStudentIdOrderByCreatedAtDesc(eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        AdminUserDetailResponse result = adminService.getUserDetail(2L);

        assertThat(result.studentProfile()).isNull();
        assertThat(result.matches()).isEmpty();
    }

    @Test
    @DisplayName("Olmayan kullanıcı için exception fırlatılmalı")
    void getUserDetail_user_not_found_throws_exception() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.USER_NOT_FOUND);

        verify(studentProfileRepository, never()).findByUserId(any());
        verify(matchRequestRepository, never()).findByStudentIdOrderByCreatedAtDesc(any(), any());
    }
}
