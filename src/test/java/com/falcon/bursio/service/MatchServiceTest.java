package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.MatchFilterRequest;
import com.falcon.bursio.dto.request.MatchRespondRequest;
import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.InvalidMatchStateException;
import com.falcon.bursio.exception.NoCandidatesFoundException;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.repository.StudentProfileRepository;
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
class MatchServiceTest {

    @Mock private MatchRequestRepository matchRequestRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private MatchService matchService;

    private User donor;
    private User student;
    private StudentProfile studentProfile;
    private MatchRequest pendingMatch;

    @BeforeEach
    void setUp() {
        donor = User.builder()
                .email("donor@gmail.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .role(Role.DONOR)
                .build();

        student = User.builder()
                .email("student@ogr.gazi.edu.tr")
                .firstName("Ali")
                .lastName("Kaya")
                .role(Role.STUDENT)
                .build();

        studentProfile = StudentProfile.builder()
                .user(student)
                .university(University.builder().name("Gazi Üniversitesi").build())
                .department("Bilgisayar Mühendisliği")
                .profileComplete(true)
                .build();

        pendingMatch = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.PENDING)
                .build();
    }

    // ── createRandomMatch ────────────────────────────────────────────────────

    @Test
    @DisplayName("Uygun öğrenci varsa eşleşme oluşturulmalı")
    void createRandomMatch_success() {
        MatchFilterRequest filter = new MatchFilterRequest(
                null, null, null, null, null, "Merhaba");

        when(studentProfileRepository.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(studentProfile)));
        when(matchRequestRepository.existsByDonorIdAndStudentIdAndStatus(any(), any(), any()))
                .thenReturn(false);
        when(matchRequestRepository.save(any(MatchRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                matchService.createRandomMatch(donor, filter));

        verify(matchRequestRepository).save(any(MatchRequest.class));
        verify(emailService).sendMatchRequestNotification(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Uygun öğrenci yoksa NoCandidatesFoundException fırlatılmalı")
    void createRandomMatch_no_candidates_throws_exception() {
        MatchFilterRequest filter = new MatchFilterRequest(
                null, null, null, null, null, null);

        when(studentProfileRepository.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThatThrownBy(() -> matchService.createRandomMatch(donor, filter))
                .isInstanceOf(NoCandidatesFoundException.class);

        verify(matchRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tüm adaylarla zaten PENDING eşleşme varsa exception fırlatılmalı")
    void createRandomMatch_all_candidates_pending_throws_exception() {
        MatchFilterRequest filter = new MatchFilterRequest(
                null, null, null, null, null, null);

        when(studentProfileRepository.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(studentProfile)));
        when(matchRequestRepository.existsByDonorIdAndStudentIdAndStatus(any(), any(), eq(MatchStatus.PENDING)))
                .thenReturn(true);

        assertThatThrownBy(() -> matchService.createRandomMatch(donor, filter))
                .isInstanceOf(NoCandidatesFoundException.class);
    }

    // ── respondToMatch ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Öğrenci eşleşmeyi kabul edebilmeli")
    void respondToMatch_accept_success() {
        MatchRespondRequest request = new MatchRespondRequest(MatchStatus.ACCEPTED);

        when(matchRequestRepository.findByIdAndStudentId(any(), any()))
                .thenReturn(Optional.of(pendingMatch));
        when(matchRequestRepository.save(any(MatchRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        matchService.respondToMatch(student, 1L, request);

        assertThat(pendingMatch.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
        verify(emailService).sendMatchAcceptedNotification(anyString(), anyString());
    }

    @Test
    @DisplayName("Öğrenci eşleşmeyi reddedebilmeli")
    void respondToMatch_decline_success() {
        MatchRespondRequest request = new MatchRespondRequest(MatchStatus.DECLINED);

        when(matchRequestRepository.findByIdAndStudentId(any(), any()))
                .thenReturn(Optional.of(pendingMatch));
        when(matchRequestRepository.save(any(MatchRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        matchService.respondToMatch(student, 1L, request);

        assertThat(pendingMatch.getStatus()).isEqualTo(MatchStatus.DECLINED);
        verify(emailService, never()).sendMatchAcceptedNotification(anyString(), anyString());
    }

    @Test
    @DisplayName("Başkasının eşleşmesine yanıt verilememeli")
    void respondToMatch_not_found_throws_exception() {
        MatchRespondRequest request = new MatchRespondRequest(MatchStatus.ACCEPTED);

        when(matchRequestRepository.findByIdAndStudentId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.respondToMatch(student, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.MATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("PENDING olmayan eşleşmeye yanıt verilememeli")
    void respondToMatch_already_responded_throws_exception() {
        MatchRequest acceptedMatch = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.ACCEPTED)
                .build();

        MatchRespondRequest request = new MatchRespondRequest(MatchStatus.ACCEPTED);

        when(matchRequestRepository.findByIdAndStudentId(any(), any()))
                .thenReturn(Optional.of(acceptedMatch));

        assertThatThrownBy(() -> matchService.respondToMatch(student, 1L, request))
                .isInstanceOf(InvalidMatchStateException.class)
                .hasMessage(ErrorMessages.INVALID_MATCH_STATE);
    }
}