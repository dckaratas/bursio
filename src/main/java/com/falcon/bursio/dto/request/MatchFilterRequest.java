package com.falcon.bursio.dto.request;

import com.falcon.bursio.entity.enums.DepartmentCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MatchFilterRequest(
        Long universityId,

        DepartmentCategory departmentCategory,

        @Min(value = 0, message = "Minimum sınıf 0 (hazırlık) olabilir.")
        @Max(value = 7, message = "Maximum sınıf 7 (yüksek lisans) olabilir.")
        Integer minGrade,

        @Min(value = 0, message = "Minimum sınıf 0 (hazırlık) olabilir.")
        @Max(value = 7, message = "Maximum sınıf 7 (yüksek lisans) olabilir.")
        Integer maxGrade,

        @DecimalMin(value = "0.0", message = "Minimum GPA 0.0'dan küçük olamaz.")
        @DecimalMax(value = "4.0", message = "Minimum GPA 4.0'dan büyük olamaz.")
        BigDecimal minGpa,

        @Size(max = 1000, message = "Mesaj en fazla 1000 karakter olabilir.")
        String donorMessage
) {}
