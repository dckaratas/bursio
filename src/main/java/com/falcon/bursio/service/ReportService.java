package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.common.SuccessMessages;
import com.falcon.bursio.dto.request.ReportRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.ReportStatus;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.ReportRepository;
import com.falcon.bursio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse createReport(User reporter, ReportRequest request) {

        if (reporter.getId().equals(request.reportedUserId())) {
            throw new IllegalArgumentException(ErrorMessages.CANNOT_REPORT_YOURSELF);
        }

        User reportedUser = userRepository.findById(request.reportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reason(request.reason())
                .description(request.description())
                .reportStatus(ReportStatus.OPEN)
                .build();

        reportRepository.save(report);
        return new MessageResponse(SuccessMessages.REPORT_CREATED);
    }
}