package com.falcon.bursio.repository;

import com.falcon.bursio.entity.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {

    @Query("SELECT u FROM University u JOIN u.emailDomains d WHERE d = :domain AND u.active = true")
    Optional<University> findByEmailDomain(@Param("domain") String domain);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM University u JOIN u.emailDomains d WHERE d = :domain")
    boolean existsByEmailDomain(@Param("domain") String domain);

    @Query("SELECT u FROM University u WHERE u.active = true AND " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<University> searchByNameOrCity(@Param("query") String query, Pageable pageable);

    @Query("""
                SELECT u FROM University u
                WHERE (:query IS NULL OR :query = '' OR
                       LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                       LOWER(u.city) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<University> searchUniversities(
            @Param("query") String query,
            Pageable pageable
    );
}
