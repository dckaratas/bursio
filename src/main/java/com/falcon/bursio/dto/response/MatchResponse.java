package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.enums.MatchStatus;

import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        Long donorId,
        String donorFirstName,
        String donorLastName,
        String donorEmail,
        Long studentId,
        String studentFirstName,
        String studentLastName,
        String studentEmail,
        String contactPreference,
        String contactValue,        // sadece ACCEPTED ise dolu gelir
        MatchStatus status,
        String donorMessage,
        LocalDateTime respondedAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static MatchResponse from(MatchRequest match) {
        boolean accepted = match.getStatus() == MatchStatus.ACCEPTED;

        return new MatchResponse(
                match.getId(),
                match.getDonor().getId(),
                match.getDonor().getFirstName(),
                match.getDonor().getLastName(),
                match.getDonor().getEmail(),
                match.getStudent().getId(),
                accepted ? match.getStudent().getFirstName() : "—",
                accepted ? match.getStudent().getLastName() : "—",
                accepted ? match.getStudent().getEmail() : "—",
                accepted && match.getStudent().getStudentProfile() != null
                        ? match.getStudent().getStudentProfile().getContactPreference().name()
                        : null,
                accepted && match.getStudent().getStudentProfile() != null
                        ? match.getStudent().getStudentProfile().getContactValue()
                        : null,
                match.getStatus(),
                match.getDonorMessage(),
                match.getRespondedAt(),
                match.getExpiresAt(),
                match.getCreatedAt()
        );
    }
}