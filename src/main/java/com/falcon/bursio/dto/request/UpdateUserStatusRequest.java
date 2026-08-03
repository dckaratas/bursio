package com.falcon.bursio.dto.request;

import com.falcon.bursio.entity.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Durum boş olamaz.")
        AccountStatus status
) {}