package com.falcon.bursio.repository;

import com.falcon.bursio.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {

    @Query("SELECT u FROM University u JOIN u.emailDomains d WHERE d = :domain AND u.active = true")
    Optional<University> findByEmailDomain(@Param("domain") String domain);
}
