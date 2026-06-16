package com.falcon.bursio.controller;

import com.falcon.bursio.dto.request.StudentProfileRequest;
import com.falcon.bursio.dto.response.StudentProfileResponse;
import com.falcon.bursio.security.SecurityUtils;
import com.falcon.bursio.service.StudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> createOrUpdate(
            @Valid @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(
                studentProfileService.createOrUpdate(SecurityUtils.getCurrentUser(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> getMyProfile() {
        return ResponseEntity.ok(
                studentProfileService.getMyProfile(SecurityUtils.getCurrentUser()));
    }
}