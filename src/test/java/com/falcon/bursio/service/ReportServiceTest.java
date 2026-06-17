package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.ReportRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.ReportReason;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User reporter;
    private User reportedUser;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .email("reporter@gmail.com")
                .role(Role.DONOR)
                .build();

        reportedUser = User.builder()
                .email("reported@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .build();

        // reporter'a id ver
        try {
            var idField = reporter.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reporter, 1L);

            var idField2 = reportedUser.getClass().getSuperclass().getDeclaredField("id");
            idField2.setAccessible(true);
            idField2.set(reportedUser, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Şikayet başarıyla oluşturulabilmeli")
    void createReport_success() {
        ReportRequest request = new ReportRequest(2L, ReportReason.SPAM, "Spam yapıyor.");

        when(userRepository.findById(2L)).thenReturn(Optional.of(reportedUser));

        MessageResponse response = reportService.createReport(reporter, request);

        assertThat(response.message()).isNotBlank();
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("Kendini şikayet edilememeli")
    void createReport_self_report_throws_exception() {
        ReportRequest request = new ReportRequest(1L, ReportReason.SPAM, "Kendim.");

        assertThatThrownBy(() -> reportService.createReport(reporter, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.CANNOT_REPORT_YOURSELF);

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Olmayan kullanıcı şikayet edilememeli")
    void createReport_user_not_found_throws_exception() {
        ReportRequest request = new ReportRequest(99L, ReportReason.FAKE_PROFILE, "Sahte profil.");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createReport(reporter, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.USER_NOT_FOUND);

        verify(reportRepository, never()).save(any());
    }
}