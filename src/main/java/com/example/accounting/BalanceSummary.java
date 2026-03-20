package com.example.accounting;

import java.math.BigDecimal;

public record BalanceSummary(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
