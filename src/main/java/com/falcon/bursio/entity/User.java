package com.falcon.bursio.entity;

import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "studentProfile")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String verificationToken;
    private LocalDateTime tokenExpiry;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private boolean privacyPolicyAccepted = false;

    @Column(nullable = false)
    private boolean termsAccepted = false;

    private LocalDateTime consentAcceptedAt;

    private LocalDateTime lastVerificationSentAt;

    private String passwordResetToken;

    private LocalDateTime passwordResetExpiry;

    private LocalDateTime emailVerifiedAt;
}
