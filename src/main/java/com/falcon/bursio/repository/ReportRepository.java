package com.falcon.bursio.repository;

import com.falcon.bursio.entity.Report;
import com.falcon.bursio.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReportStatus(ReportStatus status, Pageable pageable);

    List<Report> findByReporterId(Long reporterId);

    List<Report> findByReportedUserId(Long reportedUserId);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.reporter IS NULL AND r.reportedUser IS NULL")
    void deleteByReporterIsNullAndReportedUserIsNull();

    @Query("""
                SELECT r FROM Report r
                WHERE (:status IS NULL OR r.reportStatus = :status)
                AND (:query IS NULL OR :query = '' OR
                     LOWER(r.reporterEmail) LIKE LOWER(CONCAT('%', :query, '%')) OR
                     LOWER(r.reportedUserEmail) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Report> searchReports(
            @Param("query") String query,
            @Param("status") ReportStatus status,
            Pageable pageable
    );
}