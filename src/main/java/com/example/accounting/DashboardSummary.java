package com.example.accounting;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
        long transactionCount,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance,
        List<Transaction> recentTransactions
) {
}
