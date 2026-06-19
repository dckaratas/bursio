package com.falcon.bursio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email boş olamaz.")
        @Email(message = "Geçerli bir email adresi giriniz.")
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Geçerli bir email adresi giriniz."
        )
        String email
) {}