package com.falcon.bursio.repository;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<MatchRequest> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    Optional<MatchRequest> findByIdAndStudentId(Long id, Long studentId);

    boolean existsByDonorIdAndStudentIdAndStatus(Long donorId, Long studentId, MatchStatus status);
}