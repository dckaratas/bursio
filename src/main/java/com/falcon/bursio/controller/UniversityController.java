package com.falcon.bursio.controller;

import com.falcon.bursio.dto.response.PageResponse;
import com.falcon.bursio.dto.response.UniversityResponse;
import com.falcon.bursio.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping
    public ResponseEntity<PageResponse<UniversityResponse>> getUniversities(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(universityService.getUniversities(query, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<UniversityResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(universityService.searchUniversities(query, size));
    }

    @GetMapping("/by-domain")
    public ResponseEntity<UniversityResponse> getByDomain(@RequestParam String domain) {
        return universityService.findByDomain(domain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}