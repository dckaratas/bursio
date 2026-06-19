package com.falcon.bursio.service;

import com.falcon.bursio.common.AppConstants;
import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.common.SuccessMessages;
import com.falcon.bursio.dto.request.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

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
                .tokenExpiry(LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS))
                .privacyPolicyAccepted(request.privacyPolicyAccepted())
                .termsAccepted(request.termsAccepted())
                .consentAcceptedAt(LocalDateTime.now())
                .build();

        // Önce email göndermeyi dene
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        } catch (Exception e) {
            log.error("Email gönderilemedi: {}", user.getEmail(), e);
            throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED_VERIFICATION);
        }

        // Email başarıyla gittiyse kaydet
        userRepository.save(user);

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

    @Transactional
    public MessageResponse resendVerificationEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Zaten doğrulanmış mı?
        if (user.isEmailVerified()) {
            throw new ResendVerificationException(ErrorMessages.ACCOUNT_ALREADY_VERIFIED);
        }

        // 3 dakika cooldown kontrolü
        if (user.getLastVerificationSentAt() != null) {
            LocalDateTime cooldownEnd = user.getLastVerificationSentAt()
                    .plusMinutes(AppConstants.RESEND_VERIFICATION_COOLDOWN_MINUTES);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                throw new ResendVerificationException(ErrorMessages.RESEND_COOLDOWN);
            }
        }

        // Yeni token oluştur
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS));
        user.setLastVerificationSentAt(LocalDateTime.now());

        // Email gönder
        try {
            emailService.sendVerificationEmail(user.getEmail(), newToken);
        } catch (Exception e) {
            log.error("Doğrulama emaili gönderilemedi: {}", email, e);
            throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED);
        }

        userRepository.save(user);
        return new MessageResponse(SuccessMessages.VERIFICATION_RESENT);
    }

    @Transactional
    public MessageResponse changeEmail(ChangeEmailRequest request) {

        if (request.currentEmail().equalsIgnoreCase(request.newEmail())) {
            throw new EmailAlreadyExistsException("Yeni email adresi mevcut adresle aynı olamaz.");
        }

        User user = userRepository.findByEmail(request.currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Sadece doğrulanmamış hesaplarda değişikliğe izin ver
        if (user.isEmailVerified()) {
            throw new ResendVerificationException(ErrorMessages.EMAIL_CHANGE_NOT_ALLOWED);
        }

        // Yeni email zaten kayıtlı mı?
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        // Öğrenci ise yeni email domain kontrolü
        if (user.getRole() == Role.STUDENT) {
            String domain = extractDomain(request.newEmail());
            universityRepository.findByEmailDomain(domain)
                    .orElseThrow(() -> new InvalidUniversityEmailException(
                            ErrorMessages.INVALID_UNIVERSITY_EMAIL));
        }

        // Cooldown kontrolü
        if (user.getLastVerificationSentAt() != null) {
            LocalDateTime cooldownEnd = user.getLastVerificationSentAt()
                    .plusMinutes(AppConstants.RESEND_VERIFICATION_COOLDOWN_MINUTES);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                throw new ResendVerificationException(ErrorMessages.EMAIL_CHANGE_COOLDOWN);
            }
        }

        // Email güncelle, yeni token oluştur
        String newToken = UUID.randomUUID().toString();
        user.setEmail(request.newEmail());
        user.setVerificationToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS));
        user.setLastVerificationSentAt(LocalDateTime.now());

        // Yeni adrese doğrulama emaili gönder
        try {
            emailService.sendVerificationEmail(request.newEmail(), newToken);
        } catch (Exception e) {
            log.error("Email gönderilemedi: {}", request.newEmail(), e);
            throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED_VERIFICATION);
        }

        userRepository.save(user);
        return new MessageResponse(SuccessMessages.EMAIL_CHANGED);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Cooldown kontrolü
        if (user.getPasswordResetToken() != null && user.getPasswordResetExpiry() != null) {
            LocalDateTime cooldownEnd = user.getPasswordResetExpiry()
                    .minusHours(AppConstants.PASSWORD_RESET_TOKEN_EXPIRY_HOURS)
                    .plusMinutes(AppConstants.PASSWORD_RESET_COOLDOWN_MINUTES);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                throw new ResendVerificationException(ErrorMessages.PASSWORD_RESET_COOLDOWN);
            }
        }

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(
                LocalDateTime.now().plusHours(AppConstants.PASSWORD_RESET_TOKEN_EXPIRY_HOURS));

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (Exception e) {
            log.error("Şifre sıfırlama emaili gönderilemedi: {}", user.getEmail(), e);
            throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED);
        }

        userRepository.save(user);
        return new MessageResponse(SuccessMessages.PASSWORD_RESET_SENT);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new InvalidTokenException(ErrorMessages.INVALID_RESET_TOKEN));

        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException(ErrorMessages.INVALID_RESET_TOKEN);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);

        return new MessageResponse(SuccessMessages.PASSWORD_RESET_SUCCESS);
    }
}