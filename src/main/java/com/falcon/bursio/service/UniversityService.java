package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.AddEmailDomainRequest;
import com.falcon.bursio.dto.request.AddUniversityRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.dto.response.UniversityResponse;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    // ── Üniversite Yönetimi ──────────────────────────────────────────────────

    public PageResponse<UniversityResponse> getAllUniversities(int page, int size) {
        return PageResponse.from(
                universityRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()))
                        .map(UniversityResponse::from)
        );
    }

    @Transactional
    public UniversityResponse addUniversity(AddUniversityRequest request) {
        for (String domain : request.emailDomains()) {
            if (universityRepository.existsByEmailDomain(domain)) {
                throw new IllegalArgumentException(
                        "'" + domain + "' " + ErrorMessages.DOMAIN_ALREADY_EXISTS);
            }
        }

        University university = University.builder()
                .name(request.name())
                .city(request.city())
                .emailDomains(request.emailDomains().stream()
                        .map(String::toLowerCase)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .active(true)
                .build();

        universityRepository.save(university);
        return UniversityResponse.from(university);
    }

    @Transactional
    public UniversityResponse addEmailDomain(Long universityId, AddEmailDomainRequest request) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UNIVERSITY_NOT_FOUND));

        String domain = request.domain().toLowerCase();

        if (university.getEmailDomains().contains(domain)) {
            throw new IllegalArgumentException(ErrorMessages.DOMAIN_ALREADY_IN_UNIVERSITY);
        }

        if (universityRepository.existsByEmailDomain(domain)) {
            throw new IllegalArgumentException(
                    "'" + domain + "' " + ErrorMessages.DOMAIN_ALREADY_EXISTS);
        }

        university.getEmailDomains().add(domain);
        universityRepository.save(university);
        return UniversityResponse.from(university);
    }

    @Transactional
    public MessageResponse toggleUniversityStatus(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UNIVERSITY_NOT_FOUND));

        university.setActive(!university.isActive());
        universityRepository.save(university);

        return new MessageResponse(university.isActive()
                ? "Üniversite aktif edildi."
                : "Üniversite pasif yapıldı.");
    }

    public PageResponse<UniversityResponse> searchUniversities(String query, int size) {
        if (query == null || query.isBlank()) {
            return getAllUniversities(0, size);
        }
        return PageResponse.from(
                universityRepository
                        .searchByNameOrCity(query, PageRequest.of(0, size, Sort.by("name")))
                        .map(UniversityResponse::from)
        );
    }

    public PageResponse<UniversityResponse> getUniversities(String query, int page, int size) {
        return PageResponse.from(
                universityRepository.searchUniversities(
                                query,
                                PageRequest.of(page, size, Sort.by("name").ascending()))
                        .map(UniversityResponse::from)
        );
    }

    public Optional<UniversityResponse> findByDomain(String domain) {
        return universityRepository.findByEmailDomain(domain)
                .map(UniversityResponse::from);
    }
}