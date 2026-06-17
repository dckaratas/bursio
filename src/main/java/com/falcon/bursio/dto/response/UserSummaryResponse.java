package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        AccountStatus status,
        boolean emailVerified,
        LocalDateTime createdAt
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}