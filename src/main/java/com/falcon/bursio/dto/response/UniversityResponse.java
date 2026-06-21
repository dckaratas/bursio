package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.University;

import java.util.List;

public record UniversityResponse(
        Long id,
        String name,
        String city,
        List<String> emailDomains,
        boolean active
) {
    public static UniversityResponse from(University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getCity(),
                List.copyOf(university.getEmailDomains()),
                university.isActive()
        );
    }
}