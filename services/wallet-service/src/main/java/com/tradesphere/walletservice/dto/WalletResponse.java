package com.tradesphere.walletservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String userId,
        BigDecimal balance,
        String currency,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
