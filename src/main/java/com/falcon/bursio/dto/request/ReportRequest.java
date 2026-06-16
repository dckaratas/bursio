package com.falcon.bursio.dto.request;

import com.falcon.bursio.entity.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(

        //TODO: MESAJLARI DÜZENLE
        @NotNull(message = "Şikayet edilen kullanıcı boş olamaz.")
        Long reportedUserId,

        @NotNull(message = "Şikayet nedeni boş olamaz.")
        ReportReason reason,

        @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
        String description
) {}