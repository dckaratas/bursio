package com.falcon.bursio.common;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String BASE_URL = "http://localhost:8080";
    public static final String VERIFY_EMAIL_URL = BASE_URL + "/api/auth/verify-email?token=";
    public static final long VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    public static final String STUDENT_EN = "Student";
    public static final String STUDENT = "Öğrenci";
}