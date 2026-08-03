package com.falcon.bursio.exception;

import com.falcon.bursio.dto.request.StudentProfileRequest;
import com.falcon.bursio.entity.enums.ContactPreference;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactValueValidator implements ConstraintValidator<ValidContactValue, StudentProfileRequest> {

    private static final String PHONE_REGEX = "^(\\+90|0)?[5][0-9]{9}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public boolean isValid(StudentProfileRequest request, ConstraintValidatorContext context) {
        if (request.contactPreference() == null || request.contactValue() == null) {
            return true; // diğer validasyonlar zaten yakalayacak
        }

        String value = request.contactValue().trim();

        if (request.contactPreference() == ContactPreference.PHONE) {
            if (!value.matches(PHONE_REGEX)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Geçerli bir telefon numarası giriniz. (Örn: 05XX XXX XX XX)"
                ).addConstraintViolation();
                return false;
            }
        } else if (request.contactPreference() == ContactPreference.EMAIL) {
            if (!value.matches(EMAIL_REGEX)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Geçerli bir email adresi giriniz."
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}