package com.example.accounting;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        BigDecimal amount,
        String type,
        Instant createdAt
) {
}
