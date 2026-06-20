package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.common.SuccessMessages;
import com.falcon.bursio.dto.request.DeleteAccountRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.MatchRequestRepository;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.StudentProfileRepository;
import com.falcon.bursio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MatchRequestRepository matchRequestRepository;
    private final ReportRepository reportRepository;
    private final EmailService emailService;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional
    public MessageResponse deleteAccount(User currentUser, DeleteAccountRequest request) {

        if (currentUser.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(ErrorMessages.CANNOT_DELETE_ADMIN);
        }

        if (!passwordEncoder.matches(request.password(), currentUser.getPassword())) {
            throw new ResourceNotFoundException(ErrorMessages.INVALID_PASSWORD);
        }

        // PENDING eşleşmeleri EXPIRED yap ve bildirim gönder
        List<MatchRequest> pendingMatches = matchRequestRepository
                .findPendingMatchesByUserId(currentUser.getId());

        pendingMatches.forEach(match -> {
            match.setStatus(MatchStatus.EXPIRED);
            matchRequestRepository.save(match);

            if (match.getStudent().getId().equals(currentUser.getId())) {
                emailService.sendAccountDeletedNotification(
                        match.getDonor().getEmail(),
                        """
                                <h2>Eşleşme İptali</h2>
                                <p>Burs teklif ettiğiniz öğrenci hesabını sildi.</p>
                                <p>Yeni bir eşleşme bulmak için platforma giriş yapabilirsiniz.</p>
                                """
                );
            } else {
                emailService.sendAccountDeletedNotification(
                        match.getStudent().getEmail(),
                        """
                                <h2>Eşleşme İptali</h2>
                                <p>Burs teklifini aldığınız kullanıcı hesabını sildi.</p>
                                <p>Platformda yeni teklifler sizi bekliyor olabilir.</p>
                                """
                );
            }
        });

        // Diğer eşleşmeleri sil
        matchRequestRepository.deleteByUserId(currentUser.getId());

        // Reporter silinirse raporu koru (reportedUser varsa)
        reportRepository.findByReporterId(currentUser.getId()).forEach(report -> {
            report.setReporter(null);
            reportRepository.save(report);
        });

        // Reported silinirse raporu koru (reporter varsa)
        reportRepository.findByReportedUserId(currentUser.getId()).forEach(report -> {
            report.setReportedUser(null);
            reportRepository.save(report);
        });

        // Her ikisi de null olan raporları sil
        reportRepository.deleteByReporterIsNullAndReportedUserIsNull();

        // StudentProfile varsa önce sil
        studentProfileRepository.findByUserId(currentUser.getId())
                .ifPresent(studentProfileRepository::delete);

        // Kullanıcıyı sil
        userRepository.delete(currentUser);

        return new MessageResponse(SuccessMessages.ACCOUNT_DELETED);
    }
}