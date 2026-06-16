package com.falcon.bursio.service;

import com.falcon.bursio.common.AppConstants;
import com.falcon.bursio.common.SuccessMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendVerificationEmail(String to, String token) {
        // TODO: SendGrid entegrasyonu eklenecek
        String link = AppConstants.VERIFY_EMAIL_URL + token;
        log.info("{}: {} | Link: {}", SuccessMessages.VERIFICATION_EMAIL_SEND_EN, to, link);
    }

    public void sendMatchAcceptedNotification(String to, String studentName) {
        // TODO: SendGrid entegrasyonu eklenecek
        log.info("{}: {} | {}: {}", SuccessMessages.MATCH_REQUEST_NOTIF_SEND_EN, to, AppConstants.STUDENT, studentName);
    }

    public void sendMatchRequestNotification(String to, String donorFirstName, String donorMessage) {
        // TODO: SendGrid entegrasyonu eklenecek
        log.info("Eşleşme talebi bildirimi gönderildi: {} | Donor: {} | Mesaj: {}",
                to, donorFirstName, donorMessage);
    }
}