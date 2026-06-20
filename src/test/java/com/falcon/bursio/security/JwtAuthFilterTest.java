package com.falcon.bursio.security;

import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.AccountStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Authorization header yoksa filtre geçilmeli, auth set edilmemeli")
    void no_auth_header_passes_through() throws Exception {
        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Geçersiz token ile istek auth set edilmeden geçmeli")
    void invalid_token_passes_without_auth() throws Exception {
        request.addHeader("Authorization", "Bearer invalid.token.here");
        when(jwtService.isTokenValid("invalid.token.here")).thenReturn(false);

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("ACTIVE kullanıcı geçerli token ile authenticate edilmeli")
    void active_user_with_valid_token_gets_authenticated() throws Exception {
        User activeUser = User.builder()
                .email("donor@gmail.com")
                .role(Role.DONOR)
                .status(AccountStatus.ACTIVE)
                .build();

        Claims claims = mock(Claims.class);
        when(claims.get("role", String.class)).thenReturn("DONOR");

        request.addHeader("Authorization", "Bearer valid.token");
        when(jwtService.isTokenValid("valid.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.token")).thenReturn(1L);
        when(jwtService.extractClaims("valid.token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(activeUser);
    }

    @Test
    @DisplayName("SUSPENDED kullanıcı geçerli token olsa bile authenticate edilmemeli")
    void suspended_user_with_valid_token_is_not_authenticated() throws Exception {
        User suspendedUser = User.builder()
                .email("suspended@gmail.com")
                .role(Role.DONOR)
                .status(AccountStatus.SUSPENDED)
                .build();

        Claims claims = mock(Claims.class);
        when(claims.get("role", String.class)).thenReturn("DONOR");

        request.addHeader("Authorization", "Bearer valid.token");
        when(jwtService.isTokenValid("valid.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.token")).thenReturn(2L);
        when(jwtService.extractClaims("valid.token")).thenReturn(claims);
        when(userRepository.findById(2L)).thenReturn(Optional.of(suspendedUser));

        jwtAuthFilter.doFilter(request, response, filterChain);

        // İstek devam etmeli ama auth set edilmemeli
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("PENDING_VERIFICATION kullanıcı authenticate edilmemeli")
    void pending_user_with_valid_token_is_not_authenticated() throws Exception {
        User pendingUser = User.builder()
                .email("pending@gmail.com")
                .role(Role.STUDENT)
                .status(AccountStatus.PENDING_VERIFICATION)
                .build();

        Claims claims = mock(Claims.class);
        when(claims.get("role", String.class)).thenReturn("STUDENT");

        request.addHeader("Authorization", "Bearer valid.token");
        when(jwtService.isTokenValid("valid.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.token")).thenReturn(3L);
        when(jwtService.extractClaims("valid.token")).thenReturn(claims);
        when(userRepository.findById(3L)).thenReturn(Optional.of(pendingUser));

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Silinmiş kullanıcının tokeni authenticate edilmemeli")
    void deleted_user_token_is_not_authenticated() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("role", String.class)).thenReturn("DONOR");

        request.addHeader("Authorization", "Bearer valid.token");
        when(jwtService.isTokenValid("valid.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.token")).thenReturn(99L);
        when(jwtService.extractClaims("valid.token")).thenReturn(claims);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
