package com.falcon.bursio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Şifre boş olamaz.")
        String password
) {}