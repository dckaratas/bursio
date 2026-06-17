package com.falcon.bursio.service;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.scheduler.MatchScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

    @Mock private MatchRequestRepository matchRequestRepository;

    @InjectMocks
    private MatchScheduler matchScheduler;

    @Test
    @DisplayName("Süresi dolmuş PENDING eşleşmeler EXPIRED yapılmalı")
    void expireOldMatches_sets_expired_status() {
        User donor = User.builder().email("donor@gmail.com").role(Role.DONOR).build();
        User student = User.builder().email("student@ogr.gazi.edu.tr").role(Role.STUDENT).build();

        MatchRequest expiredMatch = MatchRequest.builder()
                .donor(donor)
                .student(student)
                .status(MatchStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(matchRequestRepository.findExpiredMatches(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredMatch));

        matchScheduler.expireOldMatches();

        assertThat(expiredMatch.getStatus()).isEqualTo(MatchStatus.EXPIRED);
        verify(matchRequestRepository).saveAll(List.of(expiredMatch));
    }

    @Test
    @DisplayName("Süresi dolmuş eşleşme yoksa saveAll çağrılmamalı")
    void expireOldMatches_no_expired_matches_does_nothing() {
        when(matchRequestRepository.findExpiredMatches(any(LocalDateTime.class)))
                .thenReturn(List.of());

        matchScheduler.expireOldMatches();

        verify(matchRequestRepository, never()).saveAll(any());
    }
}