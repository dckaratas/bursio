package com.falcon.bursio.dto.request;

import java.math.BigDecimal;

public record MatchFilterRequest(
        Long universityId,
        String department,
        Integer minGrade,
        Integer maxGrade,
        BigDecimal minGpa,
        String donorMessage
) {}