package com.falcon.bursio.common;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String BASE_URL = "http://localhost:8080";
    public static final String VERIFY_EMAIL_URL = BASE_URL + "/api/auth/verify-email?token=";
    public static final long VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    public static final String STUDENT_EN = "Student";
    public static final String STUDENT = "Öğrenci";

    public static final long RESEND_VERIFICATION_COOLDOWN_MINUTES = 3; // 3 dakika bekleme
    public static final int MAX_RESEND_VERIFICATION_COUNT = 5; // günde max 5 kez

    public static final long PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;
    public static final long PASSWORD_RESET_COOLDOWN_MINUTES = 3;

    public static final long EMAIL_REVERIFICATION_PERIOD_MONTHS = 12;
}