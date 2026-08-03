package com.falcon.bursio.exception;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactValueValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidContactValue {
    String message() default "Geçersiz iletişim bilgisi formatı.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}