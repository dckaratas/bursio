package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.StudentProfileRequest;
import com.falcon.bursio.dto.response.StudentProfileResponse;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.StudentProfileRepository;
import com.falcon.bursio.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UniversityRepository universityRepository;

    @Transactional
    public StudentProfileResponse createOrUpdate(User currentUser, StudentProfileRequest request) {
        // Üniversiteyi emailden otomatik bul
        String domain = currentUser.getEmail().substring(currentUser.getEmail().indexOf('@') + 1);
        University university = universityRepository.findByEmailDomain(domain)
                .orElse(null); // bulunamazsa null bırak, hata fırlatma

        StudentProfile profile = studentProfileRepository
                .findByUserId(currentUser.getId())
                .orElse(StudentProfile.builder().user(currentUser).build());

        profile.setUniversity(university);
        profile.setDepartment(request.department());
        profile.setGrade(request.grade());
        profile.setGpa(request.gpa());
        profile.setBio(request.bio());
        profile.setMotivation(request.motivation());
        profile.setContactPreference(request.contactPreference());
        profile.setContactValue(request.contactValue());
        profile.setProfileComplete(true);

        studentProfileRepository.save(profile);
        return StudentProfileResponse.from(profile, false);
    }

    public StudentProfileResponse getMyProfile(User currentUser) {
        StudentProfile profile = studentProfileRepository
                .findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDENT_PROFILE_NOT_FOUND));

        return StudentProfileResponse.from(profile, false);
    }
}