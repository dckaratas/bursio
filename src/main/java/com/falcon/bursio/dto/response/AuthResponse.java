package com.falcon.bursio.dto.response;

public record AuthResponse(
        String token,
        String email,
        String role
) {}