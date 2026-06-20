package com.falcon.bursio.repository;

import com.falcon.bursio.entity.StudentProfile;
import com.falcon.bursio.entity.enums.DepartmentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("""
                SELECT sp FROM StudentProfile sp
                WHERE sp.profileComplete = true
                AND (:universityId IS NULL OR sp.university.id = :universityId)
                AND (:departmentCategory IS NULL OR sp.departmentCategory = :departmentCategory)
                AND (:minGpa IS NULL OR sp.gpa >= :minGpa)
                AND (:minGrade IS NULL OR sp.grade >= :minGrade)
                AND (:maxGrade IS NULL OR sp.grade <= :maxGrade)
            """)
    Page<StudentProfile> findByFilters(
            @Param("universityId") Long universityId,
            @Param("departmentCategory") DepartmentCategory departmentCategory,
            @Param("minGpa") java.math.BigDecimal minGpa,
            @Param("minGrade") Integer minGrade,
            @Param("maxGrade") Integer maxGrade,
            Pageable pageable
    );
}