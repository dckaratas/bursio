package com.falcon.bursio.entity;

import com.falcon.bursio.entity.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(columnDefinition = "TEXT")
    private String donorMessage;

    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;
}
