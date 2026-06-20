package com.falcon.bursio.dto.response;

import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.DepartmentCategory;

import java.math.BigDecimal;

public record StudentProfileResponse(
        Long id,
        String firstName,
        String lastName,
        Long universityId,
        String universityName,
        String city,
        String department,
        Integer grade,
        BigDecimal gpa,
        String bio,
        String motivation,
        ContactPreference contactPreference,
        String contactValue,// null gelir, eşleşme olmadan görünmez
        DepartmentCategory departmentCategory,
        boolean profileComplete
) {
    public static StudentProfileResponse from(StudentProfile sp, boolean showContact) {
        return new StudentProfileResponse(
                sp.getId(),
                sp.getUser().getFirstName(),
                sp.getUser().getLastName(),
                sp.getUniversity() != null ? sp.getUniversity().getId() : null,
                sp.getUniversity() != null ? sp.getUniversity().getName() : null,
                sp.getUniversity() != null ? sp.getUniversity().getCity() : null,
                sp.getDepartment(),
                sp.getGrade(),
                sp.getGpa(),
                sp.getBio(),
                sp.getMotivation(),
                sp.getContactPreference(),
                showContact ? sp.getContactValue() : null,
                sp.getDepartmentCategory(),
                sp.isProfileComplete()
        );
    }
}