package com.falcon.bursio.repository;

import com.falcon.bursio.entity.User;
import com.falcon.bursio.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Page<User> findAll(Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
}
