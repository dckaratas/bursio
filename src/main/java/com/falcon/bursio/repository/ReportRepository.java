package com.falcon.bursio.repository;

import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReportStatus(ReportStatus status, Pageable pageable);
}