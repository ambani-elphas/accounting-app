package com.example.accounting;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        String category,
        BigDecimal amount,
        TransactionType type,
        Instant createdAt
) {
}
