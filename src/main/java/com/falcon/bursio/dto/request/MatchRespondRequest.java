package com.falcon.bursio.dto.request;

import com.falcon.bursio.common.ErrorMessages;
import com.falcon.bursio.entity.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record MatchRespondRequest(
        @NotNull(message = ErrorMessages.ACTION_CANNOT_EMPTY)
        MatchStatus action    // ACCEPTED veya DECLINED
) {}