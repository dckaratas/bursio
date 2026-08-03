package com.falcon.bursio.controller;

import com.falcon.bursio.dto.request.MatchFilterRequest;
import com.falcon.bursio.dto.request.MatchRespondRequest;
import com.falcon.bursio.dto.response.MatchResponse;
import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.security.SecurityUtils;
import com.falcon.bursio.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // Donor rastgele öğrenci ile eşleşir
    @PostMapping("/random")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<MatchResponse> createRandomMatch(
            @Valid @RequestBody MatchFilterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.createRandomMatch(SecurityUtils.getCurrentUser(), request));
    }

    // Öğrenci talebi kabul/red eder
    @PutMapping("/{matchId}/respond")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MatchResponse> respond(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchRespondRequest request) {
        return ResponseEntity.ok(
                matchService.respondToMatch(SecurityUtils.getCurrentUser(), matchId, request));
    }

    // Donor kendi taleplerini listeler
    @GetMapping("/donor")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<PageResponse<MatchResponse>> getDonorMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                matchService.getDonorMatches(SecurityUtils.getCurrentUser(), page, size));
    }

    // Öğrenci kendi taleplerini listeler
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageResponse<MatchResponse>> getStudentMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                matchService.getStudentMatches(SecurityUtils.getCurrentUser(), page, size));
    }
}