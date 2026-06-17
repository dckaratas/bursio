package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.StudentProfileRequest;
import com.falcon.bursio.dto.response.StudentProfileResponse;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.Role;
import com.falcon.bursio.exception.ResourceNotFoundException;
import com.falcon.bursio.repository.StudentProfileRepository;
import com.falcon.bursio.repository.UniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private UniversityRepository universityRepository;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private User student;
    private University university;
    private StudentProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .email("test@ogr.gazi.edu.tr")
                .firstName("Ali")
                .lastName("Kaya")
                .role(Role.STUDENT)
                .build();

        university = University.builder()
                .name("Gazi Üniversitesi")
                .city("Ankara")
                .active(true)
                .build();

        profileRequest = new StudentProfileRequest(
                1L,
                "Bilgisayar Mühendisliği",
                3,
                new BigDecimal("3.20"),
                "Kendim hakkında kısa bilgi.",
                "Burs almak istiyorum çünkü...",
                ContactPreference.EMAIL,
                "test@gmail.com"
        );
    }

    @Test
    @DisplayName("Profil ilk kez oluşturulabilmeli")
    void createOrUpdate_creates_new_profile() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentProfileResponse response = studentProfileService.createOrUpdate(student, profileRequest);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        assertThat(response.universityName()).isEqualTo("Gazi Üniversitesi");
        assertThat(response.profileComplete()).isTrue();
        assertThat(response.contactValue()).isNull(); // eşleşme olmadan gizli
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Mevcut profil güncellenebilmeli")
    void createOrUpdate_updates_existing_profile() {
        StudentProfile existingProfile = StudentProfile.builder()
                .user(student)
                .university(university)
                .department("Elektrik Mühendisliği")
                .profileComplete(true)
                .build();

        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.of(existingProfile));
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentProfileResponse response = studentProfileService.createOrUpdate(student, profileRequest);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Geçersiz üniversite id ile profil oluşturulamamalı")
    void createOrUpdate_with_invalid_university_throws_exception() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentProfileService.createOrUpdate(student, profileRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kendi profilini getirebilmeli")
    void getMyProfile_success() {
        StudentProfile profile = StudentProfile.builder()
                .user(student)
                .university(university)
                .department("Bilgisayar Mühendisliği")
                .profileComplete(true)
                .build();

        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.of(profile));

        StudentProfileResponse response = studentProfileService.getMyProfile(student);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        assertThat(response.contactValue()).isNull();
    }

    @Test
    @DisplayName("Profil yoksa exception fırlatılmalı")
    void getMyProfile_not_found_throws_exception() {
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentProfileService.getMyProfile(student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.STUDENT_PROFILE_NOT_FOUND);
    }
}