package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.ForgotPasswordRequest;
import com.falcon.bursio.dto.request.LoginRequest;
import com.falcon.bursio.dto.request.RegisterRequest;
import com.falcon.bursio.dto.request.ResetPasswordRequest;
import com.falcon.bursio.dto.response.AuthResponse;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.*;
import com.falcon.bursio.repository.UniversityRepository;
import com.falcon.bursio.repository.UserRepository;
import com.falcon.bursio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UniversityRepository universityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private MaintenanceService maintenanceService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest studentRegisterRequest;
    private RegisterRequest donorRegisterRequest;
    private University university;
    private User activeUser;

    @BeforeEach
    void setUp() {
        studentRegisterRequest = new RegisterRequest(
                "test@ogr.gazi.edu.tr",
                "password123",
                "Ali",
                "Kaya",
                Role.STUDENT,
                true,
                true
        );

        donorRegisterRequest = new RegisterRequest(
                "donor@gmail.com",
                "password123",
                "Ahmet",
                "Yılmaz",
                Role.DONOR,
                true,
                true
        );

        university = University.builder()
                .name("Gazi Üniversitesi")
                .city("Ankara")
                .active(true)
                .build();

        activeUser = User.builder()
                .email("donor@gmail.com")
                .password("encodedPassword")
                .role(Role.DONOR)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    // ── Register ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Donor kaydı başarılı olmalı")
    void register_donor_success() {
        when(userRepository.existsByEmail(donorRegisterRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        MessageResponse response = authService.register(donorRegisterRequest);

        assertThat(response.message()).isNotBlank();
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Öğrenci geçerli üniversite emailiyle kayıt olabilmeli")
    void register_student_with_valid_university_email_success() {
        when(userRepository.existsByEmail(studentRegisterRequest.email())).thenReturn(false);
        when(universityRepository.findByEmailDomain("ogr.gazi.edu.tr")).thenReturn(Optional.of(university));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        MessageResponse response = authService.register(studentRegisterRequest);

        assertThat(response.message()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Kayıtlı email ile tekrar kayıt olunamamalı")
    void register_with_existing_email_throws_exception() {
        when(userRepository.existsByEmail(donorRegisterRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(donorRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(ErrorMessages.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Geçersiz üniversite emailiyle öğrenci kaydı yapılamamalı")
    void register_student_with_invalid_university_email_throws_exception() {
        RegisterRequest invalidRequest = new RegisterRequest(
                "test@gmail.com", "password123", "Ali", "Kaya", Role.STUDENT, true, true);

        when(userRepository.existsByEmail(invalidRequest.email())).thenReturn(false);
        when(universityRepository.findByEmailDomain("gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(InvalidUniversityEmailException.class)
                .hasMessage(ErrorMessages.INVALID_UNIVERSITY_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bakım modu aktifken kayıt yapılamamalı")
    void register_during_maintenance_throws_exception() {
        when(maintenanceService.isActive()).thenReturn(true);

        assertThatThrownBy(() -> authService.register(donorRegisterRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ErrorMessages.MAINTENANCE_MODE);

        verify(userRepository, never()).save(any());
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Geçerli bilgilerle login başarılı olmalı")
    void login_with_valid_credentials_success() {
        LoginRequest loginRequest = new LoginRequest("donor@gmail.com", "password123");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.password(), activeUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("donor@gmail.com");
        assertThat(response.role()).isEqualTo("DONOR");
    }

    @Test
    @DisplayName("Yanlış şifre ile login yapılamamalı")
    void login_with_wrong_password_throws_exception() {
        LoginRequest loginRequest = new LoginRequest("donor@gmail.com", "wrongpassword");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.password(), activeUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("Email doğrulanmamış hesapla login yapılamamalı")
    void login_with_inactive_account_throws_exception() {
        User inactiveUser = User.builder()
                .email("donor@gmail.com")
                .password("encodedPassword")
                .role(Role.DONOR)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        LoginRequest loginRequest = new LoginRequest("donor@gmail.com", "password123");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.ACCOUNT_NOT_ACTIVE);
    }

    // ── Email Doğrulama ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Geçerli token ile email doğrulama başarılı olmalı")
    void verifyEmail_with_valid_token_success() {
        User pendingUser = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .verificationToken("valid-token")
                .tokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        when(userRepository.findByVerificationToken("valid-token"))
                .thenReturn(Optional.of(pendingUser));

        MessageResponse response = authService.verifyEmail("valid-token");

        assertThat(response.message()).isNotBlank();
        assertThat(pendingUser.isEmailVerified()).isTrue();
        assertThat(pendingUser.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(pendingUser.getVerificationToken()).isNull();
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("Geçersiz token ile email doğrulama başarısız olmalı")
    void verifyEmail_with_invalid_token_throws_exception() {
        when(userRepository.findByVerificationToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("invalid-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage(ErrorMessages.INVALID_TOKEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Süresi dolmuş token ile email doğrulama başarısız olmalı")
    void verifyEmail_with_expired_token_throws_exception() {
        User pendingUser = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .verificationToken("expired-token")
                .tokenExpiry(LocalDateTime.now().minusHours(1)) // süresi dolmuş
                .build();

        when(userRepository.findByVerificationToken("expired-token"))
                .thenReturn(Optional.of(pendingUser));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage(ErrorMessages.INVALID_TOKEN);

        verify(userRepository, never()).save(any());
    }

    // ── Resend Verification ──────────────────────────────────────────────────

    @Test
    @DisplayName("Doğrulama emaili başarıyla yeniden gönderilebilmeli")
    void resendVerification_success() {
        User pendingUser = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .lastVerificationSentAt(LocalDateTime.now().minusMinutes(5))
                .build();

        when(userRepository.findByEmail("test@ogr.gazi.edu.tr")).thenReturn(Optional.of(pendingUser));

        MessageResponse response = authService.resendVerificationEmail("test@ogr.gazi.edu.tr");

        assertThat(response.message()).isNotBlank();
        verify(emailService).sendVerificationEmail(anyString(), anyString());
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("Zaten doğrulanmış hesaba resend yapılamamalı")
    void resendVerification_already_verified_throws_exception() {
        User verifiedUser = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("test@ogr.gazi.edu.tr")).thenReturn(Optional.of(verifiedUser));

        assertThatThrownBy(() -> authService.resendVerificationEmail("test@ogr.gazi.edu.tr"))
                .isInstanceOf(ResendVerificationException.class)
                .hasMessage(ErrorMessages.ACCOUNT_ALREADY_VERIFIED);

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Cooldown süresindeyken resend yapılamamalı")
    void resendVerification_in_cooldown_throws_exception() {
        User pendingUser = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .lastVerificationSentAt(LocalDateTime.now().minusMinutes(1)) // 1dk önce → hala cooldown
                .build();

        when(userRepository.findByEmail("test@ogr.gazi.edu.tr")).thenReturn(Optional.of(pendingUser));

        assertThatThrownBy(() -> authService.resendVerificationEmail("test@ogr.gazi.edu.tr"))
                .isInstanceOf(ResendVerificationException.class)
                .hasMessage(ErrorMessages.RESEND_COOLDOWN);

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    // ── Forgot Password ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Şifre sıfırlama emaili başarıyla gönderilmeli")
    void forgotPassword_success() {
        when(userRepository.findByEmail("donor@gmail.com")).thenReturn(Optional.of(activeUser));

        MessageResponse response = authService.forgotPassword(new ForgotPasswordRequest("donor@gmail.com"));

        assertThat(response.message()).isNotBlank();
        verify(emailService).sendPasswordResetEmail(eq("donor@gmail.com"), anyString());
        verify(userRepository).save(activeUser);
        // DB'ye hash kaydedilmeli, raw UUID değil
        assertThat(activeUser.getPasswordResetToken()).isNotNull();
        assertThat(activeUser.getPasswordResetToken()).hasSize(64); // SHA-256 hex = 64 char
    }

    @Test
    @DisplayName("Olmayan kullanıcı için şifre sıfırlama yapılamamalı")
    void forgotPassword_user_not_found_throws_exception() {
        when(userRepository.findByEmail("noone@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest("noone@gmail.com")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Cooldown süresindeyken ikinci şifre sıfırlama emaili gönderilememeli")
    void forgotPassword_in_cooldown_throws_exception() {
        activeUser.setPasswordResetToken("some-hash");
        // Expiry 1 saat sonra → cooldown bitmemiş
        activeUser.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByEmail("donor@gmail.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest("donor@gmail.com")))
                .isInstanceOf(ResendVerificationException.class)
                .hasMessage(ErrorMessages.PASSWORD_RESET_COOLDOWN);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Email servisi hata verirse EmailSendException fırlatılmalı")
    void forgotPassword_email_failure_throws_exception() {
        when(userRepository.findByEmail("donor@gmail.com")).thenReturn(Optional.of(activeUser));
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPasswordResetEmail(anyString(), anyString());

        assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest("donor@gmail.com")))
                .isInstanceOf(EmailSendException.class);
    }

    // ── Reset Password ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Geçerli token ile şifre sıfırlanabilmeli, token DB'den temizlenmeli")
    void resetPassword_success() throws NoSuchAlgorithmException {
        String rawToken = "test-reset-token";
        String hashedToken = sha256(rawToken);

        activeUser.setPasswordResetToken(hashedToken);
        activeUser.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByPasswordResetToken(hashedToken)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        MessageResponse response = authService.resetPassword(
                new ResetPasswordRequest(rawToken, "newPassword123"));

        assertThat(response.message()).isNotBlank();
        assertThat(activeUser.getPasswordResetToken()).isNull();
        assertThat(activeUser.getPasswordResetExpiry()).isNull();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("Geçersiz token ile şifre sıfırlanamaz")
    void resetPassword_invalid_token_throws_exception() throws NoSuchAlgorithmException {
        String hashedToken = sha256("wrong-token");
        when(userRepository.findByPasswordResetToken(hashedToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("wrong-token", "newPassword123")))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage(ErrorMessages.INVALID_RESET_TOKEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Süresi dolmuş token ile şifre sıfırlanamaz")
    void resetPassword_expired_token_throws_exception() throws NoSuchAlgorithmException {
        String rawToken = "expired-token";
        String hashedToken = sha256(rawToken);

        activeUser.setPasswordResetToken(hashedToken);
        activeUser.setPasswordResetExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByPasswordResetToken(hashedToken)).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest(rawToken, "newPassword123")))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage(ErrorMessages.INVALID_RESET_TOKEN);

        verify(userRepository, never()).save(any());
    }

    // ── Yardımcı ────────────────────────────────────────────────────────────

    private String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}