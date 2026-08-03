package com.falcon.bursio.entity;

import com.falcon.bursio.entity.enums.ContactPreference;
import com.falcon.bursio.entity.enums.DepartmentCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "student_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    private String department;
    private Integer grade;
    private BigDecimal gpa;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Enumerated(EnumType.STRING)
    private ContactPreference contactPreference;

    private String contactValue;

    @Column(nullable = false)
    private boolean profileComplete = false;

    @Enumerated(EnumType.STRING)
    private DepartmentCategory departmentCategory;
}
