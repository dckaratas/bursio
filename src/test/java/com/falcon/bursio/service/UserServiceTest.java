package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.ChangePasswordRequest;
import com.falcon.bursio.dto.request.DeleteAccountRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MatchRequestRepository matchRequestRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private EmailService emailService;
    @Mock private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private UserService userService;

    private User donor;
    private User student;
    private User admin;

    @BeforeEach
    void setUp() {
        donor = User.builder()
                .email("donor@gmail.com")
                .password("encodedPassword")
                .role(Role.DONOR)
                .status(AccountStatus.ACTIVE)
                .build();

        student = User.builder()
                .email("student@ogr.gazi.edu.tr")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .build();

        admin = User.builder()
                .email("admin@bursio.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Şifre başarıyla değiştirilebilmeli")
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "newPass123");

        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPass123", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncodedPassword");

        MessageResponse response = userService.changePassword(donor, request);

        assertThat(response.message()).isNotBlank();
        assertThat(donor.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository).save(donor);
    }

    @Test
    @DisplayName("Mevcut şifre yanlışsa değiştirilemez")
    void changePassword_wrong_current_password_throws_exception() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass123");

        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(donor, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.INVALID_PASSWORD);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Yeni şifre eskiyle aynıysa değiştirilemez")
    void changePassword_same_password_throws_exception() {
        ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "currentPass");

        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(donor, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.SAME_PASSWORD);

        verify(userRepository, never()).save(any());
    }

    // ── deleteAccount ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Donor hesabı başarıyla silinebilmeli")
    void deleteAccount_success() {
        DeleteAccountRequest request = new DeleteAccountRequest("currentPass");

        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(matchRequestRepository.findPendingMatchesByUserId(any())).thenReturn(List.of());
        when(reportRepository.findByReporterId(any())).thenReturn(List.of());
        when(reportRepository.findByReportedUserId(any())).thenReturn(List.of());
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        MessageResponse response = userService.deleteAccount(donor, request);

        assertThat(response.message()).isNotBlank();
        verify(userRepository).delete(donor);
    }

    @Test
    @DisplayName("Yanlış şifre ile hesap silinemez")
    void deleteAccount_wrong_password_throws_exception() {
        DeleteAccountRequest request = new DeleteAccountRequest("wrongPass");

        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteAccount(donor, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.INVALID_PASSWORD);

        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Admin hesabı silinemez")
    void deleteAccount_admin_throws_exception() {
        DeleteAccountRequest request = new DeleteAccountRequest("adminPass");

        assertThatThrownBy(() -> userService.deleteAccount(admin, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.CANNOT_DELETE_ADMIN);

        verify(userRepository, never()).delete(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Hesap silinince reporter'ı olan raporlar korunmalı (reporter null yapılır)")
    void deleteAccount_nullifies_reporter_on_reports() {
        DeleteAccountRequest request = new DeleteAccountRequest("currentPass");

        Report report = new Report();
        report.setReporter(donor);

        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(matchRequestRepository.findPendingMatchesByUserId(any())).thenReturn(List.of());
        when(reportRepository.findByReporterId(any())).thenReturn(List.of(report));
        when(reportRepository.findByReportedUserId(any())).thenReturn(List.of());
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.deleteAccount(donor, request);

        assertThat(report.getReporter()).isNull();
        verify(reportRepository).save(report);
        verify(userRepository).delete(donor);
    }

    @Test
    @DisplayName("Her iki taraf da hesabını silince orphan rapor cleanup çağrılmalı")
    void deleteAccount_calls_orphan_cleanup() {
        DeleteAccountRequest request = new DeleteAccountRequest("currentPass");

        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(matchRequestRepository.findPendingMatchesByUserId(any())).thenReturn(List.of());
        when(reportRepository.findByReporterId(any())).thenReturn(List.of());
        when(reportRepository.findByReportedUserId(any())).thenReturn(List.of());
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.deleteAccount(donor, request);

        // OPEN olmayan orphan raporlar temizlenmeli; OPEN olanlar admin incelemesi için korunur
        verify(reportRepository).deleteByReporterIsNullAndReportedUserIsNull();
    }
}
