package com.falcon.bursio.dto.request;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = ErrorMessages.EMAIL_CANNOT_EMPTY)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String email,

        @NotBlank(message = ErrorMessages.PASSWORD_CANNOT_EMPTY)
        @Size(min = 8, message = ErrorMessages.PASSWORD_TOO_SHORT)
        String password,

        @NotBlank(message = ErrorMessages.FIRSTNAME_CANNOT_EMPTY)
        String firstName,

        @NotBlank(message = ErrorMessages.LASTNAME_CANNOT_EMPTY)
        String lastName,

        @NotNull(message = ErrorMessages.ROLE_CANNOT_EMPTY)
        Role role
) {
}