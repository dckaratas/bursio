package com.falcon.bursio.repository;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    Page<MatchRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    Page<MatchRequest> findByDonorIdOrderByCreatedAtDesc(Long donorId, Pageable pageable);

    Optional<MatchRequest> findByIdAndStudentId(Long id, Long studentId);

    boolean existsByDonorIdAndStudentIdAndStatus(Long donorId, Long studentId, MatchStatus status);

    @Query("SELECT m FROM MatchRequest m WHERE m.status = 'PENDING' AND m.expiresAt < :now")
    List<MatchRequest> findExpiredMatches(@Param("now") LocalDateTime now);
}