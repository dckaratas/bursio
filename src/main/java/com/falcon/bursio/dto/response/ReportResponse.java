package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.enums.ReportReason;
import com.falcon.bursio.entity.enums.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        String reporterEmail,
        String reportedUserEmail,
        ReportReason reason,
        String description,
        ReportStatus reportStatus,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getEmail(),
                report.getReportedUser().getEmail(),
                report.getReason(),
                report.getDescription(),
                report.getReportStatus(),
                report.getCreatedAt()
        );
    }
}