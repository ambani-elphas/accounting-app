package com.example.accounting;

import java.util.List;

public record TransactionPage(
        List<Transaction> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
