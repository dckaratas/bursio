package com.falcon.bursio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddUniversityRequest(
        @NotBlank(message = "Üniversite adı boş olamaz.")
        String name,

        @NotBlank(message = "Şehir boş olamaz.")
        String city,

        @NotEmpty(message = "En az bir email domain girilmeli.")
        List<String> emailDomains
) {}