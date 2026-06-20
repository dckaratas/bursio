package com.falcon.bursio.dto.request;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.DepartmentCategory;
import com.falcon.bursio.exception.ValidContactValue;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@ValidContactValue
public record StudentProfileRequest(

        @NotBlank(message = ErrorMessages.DEPARTMENT_CANNOT_EMPTY)
        String department,

        @NotNull(message = ErrorMessages.GRADE_CANNOT_EMPTY)
        @Min(value = 1, message = ErrorMessages.GRADE_CANNOT_BE_SMALLER_THAN_ONE)
        @Max(value = 6, message = ErrorMessages.GRADE_CANNOT_BE_BIGGER_THAN_SIX)
        Integer grade,

        //TODO: mesaj sabitlerini taşı
        @DecimalMin(value = "0.0", message = "GPA 0.0'dan küçük olamaz.")
        @DecimalMax(value = "4.0", message = "GPA 4.0'dan büyük olamaz.")
        BigDecimal gpa,

        @NotBlank(message = "Biyografi boş olamaz.")
        @Size(max = 500, message = "Biyografi en fazla 500 karakter olabilir.")
        String bio,

        @NotBlank(message = "Motivasyon boş olamaz.")
        @Size(max = 1000, message = "Motivasyon en fazla 1000 karakter olabilir.")
        String motivation,

        @NotNull(message = "İletişim tercihi boş olamaz.")
        ContactPreference contactPreference,

        @NotBlank(message = "İletişim bilgisi boş olamaz.")
        @Size(max = 50, message = "İletişim bilgisi en fazla 50 karakter olabilir.")
        String contactValue,

        @NotNull(message = "Bölüm kategorisi boş olamaz.")
        DepartmentCategory departmentCategory
) {
}