package com.falcon.bursio.service;

import org.springframework.stereotype.Service;

/**
 * In-memory bakım modu yönetimi.
 * DB veya sunucu sorunlarında restart gerektirmeden admin panelinden toggle edilebilir.
 * Uygulama restart'ında sıfırlanır — bu kasıtlıdır.
 */
@Service
public class MaintenanceService {

    private volatile boolean active = false;
    private volatile String message = "Sistemde bakım çalışması yapılmaktadır. Kısa süre içinde hizmetinize devam edeceğiz.";

    public boolean isActive() {
        return active;
    }

    public String getMessage() {
        return message;
    }

    public void setMaintenance(boolean active, String message) {
        this.active = active;
        if (message != null && !message.isBlank()) {
            this.message = message;
        }
    }
}
