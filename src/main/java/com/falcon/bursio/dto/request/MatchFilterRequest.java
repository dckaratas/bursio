package com.falcon.bursio.dto.request;

import com.falcon.bursio.entity.enums.DepartmentCategory;

import java.math.BigDecimal;

public record MatchFilterRequest(
        Long universityId,
        DepartmentCategory departmentCategory,
        Integer minGrade,
        Integer maxGrade,
        BigDecimal minGpa,
        String donorMessage
) {}