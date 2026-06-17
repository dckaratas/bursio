package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.LoginRequest;
import com.falcon.bursio.dto.request.RegisterRequest;
import com.falcon.bursio.dto.response.AuthResponse;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.EmailAlreadyExistsException;
import com.falcon.bursio.exception.InvalidTokenException;
import com.falcon.bursio.exception.InvalidUniversityEmailException;
import com.falcon.bursio.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;

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
                Role.STUDENT
        );

        donorRegisterRequest = new RegisterRequest(
                "donor@gmail.com",
                "password123",
                "Ahmet",
                "Yılmaz",
                Role.DONOR
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
                "test@gmail.com", "password123", "Ali", "Kaya", Role.STUDENT);

        when(userRepository.existsByEmail(invalidRequest.email())).thenReturn(false);
        when(universityRepository.findByEmailDomain("gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(InvalidUniversityEmailException.class)
                .hasMessage(ErrorMessages.INVALID_UNIVERSITY_EMAIL);

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
}