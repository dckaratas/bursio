package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.AddEmailDomainRequest;
import com.falcon.bursio.dto.request.AddUniversityRequest;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.dto.response.UniversityResponse;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.UniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniversityServiceTest {

    @Mock private UniversityRepository universityRepository;

    @InjectMocks
    private UniversityService universityService;

    private University university;

    @BeforeEach
    void setUp() {
        university = University.builder()
                .name("Gazi Üniversitesi")
                .city("Ankara")
                .emailDomains(new ArrayList<>(List.of("gazi.edu.tr")))
                .active(true)
                .build();
    }

    // ── getUniversities ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Filtre olmadan tüm üniversiteler dönmeli (active=null)")
    void getUniversities_no_filter_returns_all() {
        University passive = University.builder()
                .name("Pasif Üniversitesi")
                .city("İzmir")
                .emailDomains(new ArrayList<>(List.of("pasif.edu.tr")))
                .active(false)
                .build();

        when(universityRepository.searchUniversities(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(university, passive)));

        PageResponse<UniversityResponse> result = universityService.getUniversities(null, null, 0, 20);

        assertThat(result.content()).hasSize(2);
        verify(universityRepository).searchUniversities(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("active=true filtresiyle yalnızca aktif üniversiteler dönmeli")
    void getUniversities_active_filter_returns_only_active() {
        when(universityRepository.searchUniversities(isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(university)));

        PageResponse<UniversityResponse> result = universityService.getUniversities(null, true, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Gazi Üniversitesi");
        verify(universityRepository).searchUniversities(isNull(), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("active=false filtresiyle yalnızca pasif üniversiteler dönmeli")
    void getUniversities_inactive_filter_returns_only_passive() {
        University passive = University.builder()
                .name("Pasif Üniversitesi")
                .city("İzmir")
                .emailDomains(new ArrayList<>(List.of("pasif.edu.tr")))
                .active(false)
                .build();

        when(universityRepository.searchUniversities(isNull(), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(passive)));

        PageResponse<UniversityResponse> result = universityService.getUniversities(null, false, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Pasif Üniversitesi");
        verify(universityRepository).searchUniversities(isNull(), eq(false), any(Pageable.class));
    }

    @Test
    @DisplayName("Query ve active filtresi birlikte çalışmalı")
    void getUniversities_query_and_active_filter_together() {
        when(universityRepository.searchUniversities(eq("Gazi"), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(university)));

        PageResponse<UniversityResponse> result = universityService.getUniversities("Gazi", true, 0, 20);

        assertThat(result.content()).hasSize(1);
        verify(universityRepository).searchUniversities(eq("Gazi"), eq(true), any(Pageable.class));
    }

    // ── addUniversity ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Yeni domain ile üniversite başarıyla eklenmeli")
    void addUniversity_success() {
        AddUniversityRequest request = new AddUniversityRequest(
                "Test Üniversitesi", "Ankara", List.of("test.edu.tr"));

        when(universityRepository.existsByEmailDomain("test.edu.tr")).thenReturn(false);
        when(universityRepository.save(any(University.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UniversityResponse response = universityService.addUniversity(request);

        assertThat(response.name()).isEqualTo("Test Üniversitesi");
        verify(universityRepository).save(any(University.class));
    }

    @Test
    @DisplayName("Başka üniversitede kayıtlı domain ile üniversite eklenemez")
    void addUniversity_domain_already_exists_throws_exception() {
        AddUniversityRequest request = new AddUniversityRequest(
                "Test Üniversitesi", "Ankara", List.of("gazi.edu.tr", "test.edu.tr"));

        when(universityRepository.existsByEmailDomain("gazi.edu.tr")).thenReturn(true);

        assertThatThrownBy(() -> universityService.addUniversity(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ErrorMessages.DOMAIN_ALREADY_EXISTS);

        verify(universityRepository, never()).save(any());
    }

    // ── addEmailDomain ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Yeni domain üniversiteye eklenebilmeli")
    void addEmailDomain_success() {
        AddEmailDomainRequest request = new AddEmailDomainRequest("ogr.gazi.edu.tr");

        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityRepository.existsByEmailDomain("ogr.gazi.edu.tr")).thenReturn(false);
        when(universityRepository.save(any(University.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UniversityResponse response = universityService.addEmailDomain(1L, request);

        assertThat(response.emailDomains()).contains("ogr.gazi.edu.tr");
    }

    @Test
    @DisplayName("Aynı üniversitede zaten kayıtlı domain tekrar eklenemez")
    void addEmailDomain_duplicate_in_same_university_throws_exception() {
        AddEmailDomainRequest request = new AddEmailDomainRequest("gazi.edu.tr"); // zaten listede

        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));

        assertThatThrownBy(() -> universityService.addEmailDomain(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.DOMAIN_ALREADY_IN_UNIVERSITY);

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Başka üniversitede kayıtlı domain eklenemez")
    void addEmailDomain_domain_used_by_another_university_throws_exception() {
        AddEmailDomainRequest request = new AddEmailDomainRequest("baska.edu.tr");

        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityRepository.existsByEmailDomain("baska.edu.tr")).thenReturn(true);

        assertThatThrownBy(() -> universityService.addEmailDomain(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ErrorMessages.DOMAIN_ALREADY_EXISTS);

        verify(universityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Üniversite bulunamazsa ResourceNotFoundException fırlatılmalı")
    void addEmailDomain_university_not_found_throws_exception() {
        when(universityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                universityService.addEmailDomain(99L, new AddEmailDomainRequest("test.edu.tr")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.UNIVERSITY_NOT_FOUND);
    }

    // ── toggleUniversityStatus ───────────────────────────────────────────────

    @Test
    @DisplayName("Aktif üniversite pasif yapılabilmeli")
    void toggleUniversityStatus_active_to_passive() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = universityService.toggleUniversityStatus(1L);

        assertThat(university.isActive()).isFalse();
        assertThat(response.message()).contains("pasif");
    }

    @Test
    @DisplayName("Pasif üniversite aktif yapılabilmeli")
    void toggleUniversityStatus_passive_to_active() {
        university.setActive(false);
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = universityService.toggleUniversityStatus(1L);

        assertThat(university.isActive()).isTrue();
        assertThat(response.message()).contains("aktif");
    }
}
