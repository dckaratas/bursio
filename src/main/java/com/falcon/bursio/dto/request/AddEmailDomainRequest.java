package com.falcon.bursio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddEmailDomainRequest(
        @NotBlank(message = "Domain boş olamaz.")
        String domain
) {}