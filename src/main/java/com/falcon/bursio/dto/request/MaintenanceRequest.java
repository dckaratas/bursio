package com.falcon.bursio.dto.request;

public record MaintenanceRequest(
        boolean active,
        String message
) {}
