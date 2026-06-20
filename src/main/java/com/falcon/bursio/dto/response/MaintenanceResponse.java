package com.falcon.bursio.dto.response;

public record MaintenanceResponse(
        boolean active,
        String message
) {}
