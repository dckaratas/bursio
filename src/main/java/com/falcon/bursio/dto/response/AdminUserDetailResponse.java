package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        AccountStatus status,
        boolean emailVerified,
        LocalDateTime createdAt,
        StudentProfileResponse studentProfile,  // null → donor veya profil tamamlanmamış
        List<MatchSummary> matches
) {
    public record MatchSummary(
            Long id,
            String counterpartName,  // öğrenciden bakınca donor adı, donörden bakınca öğrenci adı
            String counterpartEmail,
            MatchStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        public static MatchSummary fromStudentPerspective(MatchRequest m) {
            return new MatchSummary(
                    m.getId(),
                    m.getDonor().getFirstName() + " " + m.getDonor().getLastName(),
                    m.getDonor().getEmail(),
                    m.getStatus(),
                    m.getCreatedAt(),
                    m.getExpiresAt()
            );
        }

        public static MatchSummary fromDonorPerspective(MatchRequest m) {
            boolean accepted = m.getStatus() == MatchStatus.ACCEPTED;
            String name = accepted
                    ? m.getStudent().getFirstName() + " " + m.getStudent().getLastName()
                    : "Kimlik Gizli";
            String email = accepted ? m.getStudent().getEmail() : "—";
            return new MatchSummary(
                    m.getId(),
                    name,
                    email,
                    m.getStatus(),
                    m.getCreatedAt(),
                    m.getExpiresAt()
            );
        }
    }

    public static AdminUserDetailResponse from(User user,
                                                StudentProfileResponse profile,
                                                List<MatchRequest> matches) {
        boolean isStudent = user.getRole() == Role.STUDENT;

        List<MatchSummary> matchSummaries = matches.stream()
                .map(m -> isStudent
                        ? MatchSummary.fromStudentPerspective(m)
                        : MatchSummary.fromDonorPerspective(m))
                .toList();

        return new AdminUserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                profile,
                matchSummaries
        );
    }
}
