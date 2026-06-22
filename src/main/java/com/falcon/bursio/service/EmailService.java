package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.exception.EmailSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestClient restClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/api/auth/verify-email?token=" + token;

        String body = """
                <h2>BursIO'ya Hoş Geldiniz!</h2>
                <p>Hesabınızı doğrulamak için aşağıdaki bağlantıya tıklayın:</p>
                <a href="%s" style="
                    background-color: #1E40AF;
                    color: white;
                    padding: 12px 24px;
                    text-decoration: none;
                    border-radius: 6px;
                    display: inline-block;
                    margin: 16px 0;
                ">Hesabımı Doğrula</a>
                <p>Bu bağlantı 24 saat geçerlidir.</p>
                <p>Eğer bu hesabı siz oluşturmadıysanız bu emaili görmezden gelebilirsiniz.</p>
                """.formatted(link);

        sendEmail(to, "BursIO - Email Doğrulama", body);
        log.info("Email gönderildi: {} | Subject: BursIO - Email Doğrulama", to);
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendMatchRequestNotification(String to, String donorFirstName, String donorMessage) {
        String body = """
                <h2>Yeni Bir Burs Talebi Aldınız!</h2>
                <p><strong>%s</strong> size burs teklif etmek istiyor.</p>
                %s
                <p>Teklifi kabul veya reddetmek için uygulamaya giriş yapın.</p>
                """.formatted(
                donorFirstName,
                donorMessage != null
                        ? "<p><strong>Mesaj:</strong> " + donorMessage + "</p>"
                        : ""
        );

        sendEmail(to, "BursIO - Yeni Burs Talebi", body);
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendMatchAcceptedNotification(String to, String studentName) {
        String body = """
                <h2>Burs Talebiniz Kabul Edildi!</h2>
                <p><strong>%s</strong> burs teklifinizi kabul etti.</p>
                <p>İletişim bilgilerine ulaşmak için uygulamaya giriş yapın.</p>
                """.formatted(studentName);

        sendEmail(to, "BursIO - Burs Talebiniz Kabul Edildi", body);
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String body = """
            <h2>Şifre Sıfırlama</h2>
            <p>Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:</p>
            <a href="%s" style="
                background-color: #1E40AF;
                color: white;
                padding: 12px 24px;
                text-decoration: none;
                border-radius: 6px;
                display: inline-block;
                margin: 16px 0;
            ">Şifremi Sıfırla</a>
            <p>Bu bağlantı 1 saat geçerlidir.</p>
            <p>Bu işlemi siz yapmadıysanız emaili görmezden gelebilirsiniz.</p>
            """.formatted(link);
        sendEmail(to, "BursIO - Şifre Sıfırlama", body);
    }

    @Recover
    public void recoverPasswordReset(Exception e, String to, String token) {
        log.error("Şifre sıfırlama emaili gönderilemedi (3 deneme): {}", to);
        throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED);
    }

    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendAccountDeletedNotification(String to, String message) {
        sendEmail(to, "BursIO - Bildirim", message);
    }

    @Recover
    public void recoverAccountDeleted(Exception e, String to, String message) {
        log.error("Hesap silme bildirimi gönderilemedi: {}", to);
    }

    @Recover
    public void recover(Exception e, String to, String subject, String htmlBody) {
        log.error("Email gönderilemedi (3 deneme tükendi): {} | Hata: {}", to, e.getMessage());
        throw new EmailSendException(ErrorMessages.EMAIL_SEND_FAILED);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlBody
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email gönderildi: {} | Subject: {}", to, subject);
        } catch (Exception e) {
            log.warn("Email gönderimi başarısız, retry deneniyor: {} | Hata: {}", to, e.getMessage());
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage());
        }
    }
}
