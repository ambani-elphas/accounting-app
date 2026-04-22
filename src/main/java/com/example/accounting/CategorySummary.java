package com.example.accounting;

import java.math.BigDecimal;

public record CategorySummary(
        String category,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
