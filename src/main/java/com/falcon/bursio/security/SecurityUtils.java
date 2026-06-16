package com.falcon.bursio.security;

import com.falcon.bursio.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}