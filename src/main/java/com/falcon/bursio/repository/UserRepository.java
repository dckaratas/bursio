package com.falcon.bursio.repository;

import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    Optional<User> findByPasswordResetToken(String token);

    @Query("""
                SELECT u FROM User u
                WHERE (:query IS NULL OR :query = '' OR
                       LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
                       LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                       LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))
                AND (:role IS NULL OR u.role = :role)
            """)
    Page<User> searchUsers(
            @Param("query") String query,
            @Param("role") Role role,
            Pageable pageable
    );
}
