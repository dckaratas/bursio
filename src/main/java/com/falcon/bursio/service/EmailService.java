package com.falcon.bursio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.List;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

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
    }

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

    public void sendMatchAcceptedNotification(String to, String studentName) {
        String body = """
                <h2>Burs Talebiniz Kabul Edildi!</h2>
                <p><strong>%s</strong> burs teklifinizi kabul etti.</p>
                <p>İletişim bilgilerine ulaşmak için uygulamaya giriş yapın.</p>
                """.formatted(studentName);

        sendEmail(to, "BursIO - Burs Talebiniz Kabul Edildi", body);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        ApiClient client = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) client.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi(client);

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        ;
        sender.setEmail(fromEmail);
        sender.setName(fromName);

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(to);


        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(sender);
        email.setTo(List.of(recipient));
        email.setSubject(subject);
        email.setHtmlContent(htmlBody);

        try {
            api.sendTransacEmail(email);
            log.info("Email gönderildi: {} | Subject: {}", to, subject);
        } catch (ApiException e) {
            log.error("Email gönderilemedi: {} | Hata: {}", to, e.getMessage());
        }
    }
}