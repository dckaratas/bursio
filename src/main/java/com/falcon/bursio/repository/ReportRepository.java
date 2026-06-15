package com.falcon.bursio.repository;

import com.falcon.bursio.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}