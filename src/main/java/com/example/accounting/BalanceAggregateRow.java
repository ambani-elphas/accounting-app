package com.example.accounting;

import java.math.BigDecimal;

public interface BalanceAggregateRow {
    BigDecimal getIncome();

    BigDecimal getExpenses();
}
