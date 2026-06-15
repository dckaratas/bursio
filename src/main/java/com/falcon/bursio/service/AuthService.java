package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.common.SuccessMessages;
import com.falcon.bursio.dto.request.LoginRequest;
import com.falcon.bursio.dto.request.RegisterRequest;
import com.falcon.bursio.dto.response.AuthResponse;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.*;
import com.falcon.bursio.repository.UniversityRepository;
import com.falcon.bursio.repository.UserRepository;
import com.falcon.bursio.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {

        // Email daha önce kayıtlı mı?
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        // Öğrenci ise üniversite email kontrolü yap
        if (request.role() == Role.STUDENT) {
            String domain = extractDomain(request.email());
            universityRepository.findByEmailDomain(domain)
                    .orElseThrow(() -> new InvalidUniversityEmailException(
                            ErrorMessages.INVALID_UNIVERSITY_EMAIL));
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .tokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return new MessageResponse(SuccessMessages.REGISTER_SUCCESS);
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException(ErrorMessages.INVALID_TOKEN));

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException(ErrorMessages.INVALID_TOKEN);
        }

        user.setEmailVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse(SuccessMessages.EMAIL_VERIFIED);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResourceNotFoundException(ErrorMessages.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResourceNotFoundException(ErrorMessages.ACCOUNT_NOT_ACTIVE);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    private String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }
}