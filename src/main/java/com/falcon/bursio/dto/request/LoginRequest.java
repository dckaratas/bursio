package com.falcon.bursio.dto.request;

import com.falcon.bursio.common.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = ErrorMessages.EMAIL_CANNOT_EMPTY)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String email,

        @NotBlank(message = ErrorMessages.PASSWORD_CANNOT_EMPTY)
        String password
) {
}