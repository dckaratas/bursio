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
        String reporterEmail = report.getReporterEmail() != null
                ? report.getReporterEmail()
                : (report.getReporter() != null ? report.getReporter().getEmail() : "Bilinmiyor");

        String reportedUserEmail = report.getReportedUserEmail() != null
                ? report.getReportedUserEmail()
                : (report.getReportedUser() != null ? report.getReportedUser().getEmail() : "Bilinmiyor");

        boolean reporterDeleted = report.getReporter() == null;
        boolean reportedDeleted = report.getReportedUser() == null;

        return new ReportDetailResponse(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                reporterDeleted ? reporterEmail + " (Silinmiş)" : reporterEmail,
                report.getReportedUser() != null ? report.getReportedUser().getId() : null,
                reportedDeleted ? reportedUserEmail + " (Silinmiş)" : reportedUserEmail,
                report.getReason(),
                report.getDescription(),
                report.getReportStatus(),
                report.getCreatedAt()
        );
    }
}