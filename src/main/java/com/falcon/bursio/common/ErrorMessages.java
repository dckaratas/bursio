package com.falcon.bursio.common;

public final class ErrorMessages {

    private ErrorMessages() {} // instantiate edilemesin

    // Auth
    public static final String EMAIL_ALREADY_EXISTS = "Bu email adresi zaten kayıtlı.";
    public static final String INVALID_UNIVERSITY_EMAIL = "Bu email adresi kayıtlı bir üniversiteye ait değil.";
    public static final String INVALID_TOKEN = "Geçersiz veya süresi dolmuş token.";
    public static final String INVALID_CREDENTIALS = "Email veya şifre hatalı.";

    //Auth Validations
    public static final String EMAIL_CANNOT_EMPTY = "Email adresi boş olamaz.";
    public static final String INVALID_EMAIL = "Geçerli bir email adresi giriniz.";
    public static final String PASSWORD_CANNOT_EMPTY = "Şifre boş olamaz.";
    public static final String PASSWORD_TOO_SHORT = "Şifre en az 8 karakter olmalıdır.";
    public static final String FIRSTNAME_CANNOT_EMPTY = "Ad boş olamaz.";
    public static final String LASTNAME_CANNOT_EMPTY = "Soyadı boş olamaz.";
    public static final String ROLE_CANNOT_EMPTY = "Rol boş olamaz.";


    // User
    public static final String USER_NOT_FOUND = "Kullanıcı bulunamadı.";
    public static final String ACCOUNT_NOT_ACTIVE = "Hesabınız henüz aktif değil. Lütfen emailinizi doğrulayın.";

    // Student
    public static final String STUDENT_PROFILE_NOT_FOUND = "Öğrenci profili bulunamadı.";
    public static final String NO_CANDIDATES_FOUND = "Seçilen filtrelere uygun öğrenci bulunamadı.";

    // Match
    public static final String MATCH_NOT_FOUND = "Eşleşme talebi bulunamadı.";
    public static final String INVALID_MATCH_STATE = "Bu eşleşme talebi artık yanıtlanamaz.";
    public static final String DUPLICATE_MATCH = "Bu öğrenciye zaten bekleyen bir talebiniz var.";

    // Report
    public static final String REPORT_NOT_FOUND = "Şikayet bulunamadı.";
    public static final String CANNOT_REPORT_YOURSELF = "Kendinizi şikayet edemezsiniz.";

    // General
    public static final String INTERNAL_ERROR = "Beklenmeyen bir hata oluştu.";
    public static final String ACCESS_DENIED = "Bu işlem için yetkiniz yok.";
}