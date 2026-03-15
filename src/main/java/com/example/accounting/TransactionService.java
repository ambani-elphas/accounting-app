package com.example.accounting;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransactionService {

    private final List<Transaction> transactions = new ArrayList<>();

    public synchronized List<Transaction> getAll(@Nullable TransactionType type, @Nullable String category) {
        return transactions.stream()
                .filter(transaction -> type == null || transaction.type() == type)
                .filter(transaction -> category == null || transaction.category().equalsIgnoreCase(category.trim()))
                .toList();
    }

    public synchronized Transaction getById(UUID id) {
        return transactions.stream()
                .filter(transaction -> transaction.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public synchronized Transaction create(TransactionRequest request) {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                Instant.now());
        transactions.add(transaction);
        return transaction;
    }

    public synchronized void deleteById(UUID id) {
        boolean removed = transactions.removeIf(transaction -> transaction.id().equals(id));
        if (!removed) {
            throw new TransactionNotFoundException(id);
        }
    }

    public synchronized BalanceSummary getBalanceSummary() {
        BigDecimal income = transactions.stream()
                .filter(transaction -> transaction.type() == TransactionType.INCOME)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = transactions.stream()
                .filter(transaction -> transaction.type() == TransactionType.EXPENSE)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BalanceSummary(income, expenses, income.subtract(expenses));
    }

    private String normalizeCategory(String category) {
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
