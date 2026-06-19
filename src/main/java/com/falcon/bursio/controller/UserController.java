package com.falcon.bursio.controller;

import com.falcon.bursio.dto.request.DeleteAccountRequest;
import com.falcon.bursio.dto.response.MessageResponse;
import com.falcon.bursio.security.SecurityUtils;
import com.falcon.bursio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/account")
    public ResponseEntity<MessageResponse> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request) {
        return ResponseEntity.ok(
                userService.deleteAccount(SecurityUtils.getCurrentUser(), request));
    }
}