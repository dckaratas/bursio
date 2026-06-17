package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.enums.ReportReason;
import com.falcon.bursio.entity.enums.ReportStatus;

import java.time.LocalDateTime;

public record ReportDetailResponse(
        Long id,
        Long reporterId,
        String reporterEmail,
        Long reportedUserId,
        String reportedUserEmail,
        ReportReason reason,
        String description,
        ReportStatus reportStatus,
        LocalDateTime createdAt
) {
    public static ReportDetailResponse from(Report report) {
        return new ReportDetailResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getEmail(),
                report.getReportedUser().getId(),
                report.getReportedUser().getEmail(),
                report.getReason(),
                report.getDescription(),
                report.getReportStatus(),
                report.getCreatedAt()
        );
    }
}