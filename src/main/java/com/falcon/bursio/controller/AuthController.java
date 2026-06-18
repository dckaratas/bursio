package com.falcon.bursio.controller;

import com.falcon.bursio.dto.request.ChangeEmailRequest;
import com.falcon.bursio.dto.request.LoginRequest;
import com.falcon.bursio.dto.request.RegisterRequest;
import com.falcon.bursio.dto.response.AuthResponse;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerificationEmail(email));
    }

    @PutMapping("/change-email")
    public ResponseEntity<MessageResponse> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request) {
        return ResponseEntity.ok(authService.changeEmail(request));
    }
}