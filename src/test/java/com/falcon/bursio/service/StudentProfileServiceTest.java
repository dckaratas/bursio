package com.falcon.bursio.service;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.dto.request.StudentProfileRequest;
import com.falcon.bursio.dto.response.StudentProfileResponse;
import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.University;
import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.DepartmentCategory;
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

    // Emailden çıkarılan domain — servis bunu kullanır
    private static final String STUDENT_EMAIL_DOMAIN = "ogr.gazi.edu.tr";

    @BeforeEach
    void setUp() {
        student = User.builder()
                .email("test@" + STUDENT_EMAIL_DOMAIN)
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
                "Bilgisayar Mühendisliği",
                3,
                new BigDecimal("3.20"),
                "Kendim hakkında kısa bilgi.",
                "Burs almak istiyorum çünkü...",
                ContactPreference.EMAIL,
                "test@gmail.com",
                DepartmentCategory.DIGER
        );
    }

    // ── createOrUpdate ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Profil ilk kez oluşturulabilmeli, üniversite emailden belirlenmeli")
    void createOrUpdate_creates_new_profile() {
        // Servis findByEmailDomain kullanır, findById değil
        when(universityRepository.findByEmailDomain(STUDENT_EMAIL_DOMAIN))
                .thenReturn(Optional.of(university));
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentProfileResponse response = studentProfileService.createOrUpdate(student, profileRequest);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        assertThat(response.universityName()).isEqualTo("Gazi Üniversitesi");
        assertThat(response.city()).isEqualTo("Ankara");
        assertThat(response.profileComplete()).isTrue();
        // createOrUpdate showContact=false ile dönüyor
        assertThat(response.contactValue()).isNull();
        verify(studentProfileRepository).save(any(StudentProfile.class));
        // findById asla çağrılmamalı
        verify(universityRepository, never()).findById(any());
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

        when(universityRepository.findByEmailDomain(STUDENT_EMAIL_DOMAIN))
                .thenReturn(Optional.of(university));
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.of(existingProfile));
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentProfileResponse response = studentProfileService.createOrUpdate(student, profileRequest);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        assertThat(response.universityName()).isEqualTo("Gazi Üniversitesi");
        verify(studentProfileRepository).save(existingProfile);
    }

    @Test
    @DisplayName("Email domaini bilinen bir üniversiteye ait değilse üniversite null bırakılmalı")
    void createOrUpdate_unknown_domain_saves_profile_with_null_university() {
        // Servis orElse(null) kullanıyor — exception fırlatmıyor
        when(universityRepository.findByEmailDomain(STUDENT_EMAIL_DOMAIN))
                .thenReturn(Optional.empty());
        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StudentProfileResponse response = studentProfileService.createOrUpdate(student, profileRequest);

        assertThat(response.universityName()).isNull();
        assertThat(response.universityId()).isNull();
        assertThat(response.profileComplete()).isTrue();
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    // ── getMyProfile ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Kendi profilini görebilmeli, iletişim bilgisi görünür olmalı")
    void getMyProfile_success_shows_contact() {
        StudentProfile profile = StudentProfile.builder()
                .user(student)
                .university(university)
                .department("Bilgisayar Mühendisliği")
                .contactPreference(ContactPreference.EMAIL)
                .contactValue("test@gmail.com")
                .profileComplete(true)
                .build();

        when(studentProfileRepository.findByUserId(any())).thenReturn(Optional.of(profile));

        StudentProfileResponse response = studentProfileService.getMyProfile(student);

        assertThat(response.department()).isEqualTo("Bilgisayar Mühendisliği");
        // getMyProfile showContact=true ile çağrılır — kendi profilinde iletişim görünür
        assertThat(response.contactValue()).isEqualTo("test@gmail.com");
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
