package com.example.accounting;

import java.math.BigDecimal;

public interface CategoryAggregateRow {
    String getCategory();

    BigDecimal getIncome();

    BigDecimal getExpenses();
}
