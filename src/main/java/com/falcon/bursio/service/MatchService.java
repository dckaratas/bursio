package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.MatchFilterRequest;
import com.falcon.bursio.dto.request.MatchRespondRequest;
import com.falcon.bursio.dto.response.MatchResponse;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.exception.InvalidMatchStateException;
import com.falcon.bursio.exception.NoCandidatesFoundException;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRequestRepository matchRequestRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmailService emailService;

    @Transactional
    public MatchResponse createRandomMatch(User donor, MatchFilterRequest filter) {

        List<StudentProfile> candidates = studentProfileRepository.findByFilters(
                filter.universityId(),
                filter.departmentCategory(),
                filter.minGpa(),
                filter.minGrade(),
                filter.maxGrade(),
                PageRequest.of(0, 100)
        ).getContent();

        if (candidates.isEmpty()) {
            throw new NoCandidatesFoundException();
        }

        // Daha önce PENDING talebi olan öğrencileri listeden çıkar
        List<StudentProfile> eligible = candidates.stream()
                .filter(sp -> !matchRequestRepository.existsByDonorIdAndStudentIdAndStatus(
                        donor.getId(), sp.getUser().getId(), MatchStatus.PENDING))
                .toList();

        if (eligible.isEmpty()) {
            throw new NoCandidatesFoundException();
        }

        // Rastgele seç
        StudentProfile selected = eligible.get(
                ThreadLocalRandom.current().nextInt(eligible.size()));

        MatchRequest match = MatchRequest.builder()
                .donor(donor)
                .student(selected.getUser())
                .status(MatchStatus.PENDING)
                .donorMessage(filter.donorMessage())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        matchRequestRepository.save(match);

        // Öğrenciye bildirim gönder
        emailService.sendMatchRequestNotification(
                match.getStudent().getEmail(),
                match.getDonor().getFirstName(),
                match.getDonorMessage()
        );
        return MatchResponse.from(match);
    }

    @Transactional
    public MatchResponse respondToMatch(User student, Long matchId, MatchRespondRequest request) {

        MatchRequest match = matchRequestRepository
                .findByIdAndStudentId(matchId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MATCH_NOT_FOUND));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new InvalidMatchStateException(ErrorMessages.INVALID_MATCH_STATE);
        }

        if (request.action() == MatchStatus.ACCEPTED) {
            match.setStatus(MatchStatus.ACCEPTED);
            match.setRespondedAt(LocalDateTime.now());
            emailService.sendMatchAcceptedNotification(
                    match.getDonor().getEmail(),
                    match.getStudent().getFirstName()
            );
        } else if (request.action() == MatchStatus.DECLINED) {
            match.setStatus(MatchStatus.DECLINED);
            match.setRespondedAt(LocalDateTime.now());
        } else {
            throw new InvalidMatchStateException(ErrorMessages.INVALID_MATCH_STATE);
        }

        matchRequestRepository.save(match);
        return MatchResponse.from(match);
    }

    public PageResponse<MatchResponse> getDonorMatches(User donor, int page, int size) {
        return PageResponse.from(
                matchRequestRepository
                        .findByDonorIdOrderByCreatedAtDesc(
                                donor.getId(), PageRequest.of(page, size))
                        .map(MatchResponse::from)
        );
    }

    public PageResponse<MatchResponse> getStudentMatches(User student, int page, int size) {
        return PageResponse.from(
                matchRequestRepository
                        .findByStudentIdOrderByCreatedAtDesc(
                                student.getId(), PageRequest.of(page, size))
                        .map(MatchResponse::from)
        );
    }

}