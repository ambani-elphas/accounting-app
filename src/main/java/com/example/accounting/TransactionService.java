package com.example.accounting;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final List<Transaction> transactions = new ArrayList<>();

    public synchronized List<Transaction> getAll(@Nullable TransactionType type, @Nullable String category) {
        return transactions.stream()
                .filter(transaction -> type == null || transaction.type() == type)
                .filter(transaction -> category == null || transaction.category().equalsIgnoreCase(category.trim()))
                .sorted(Comparator.comparing(Transaction::createdAt).reversed())
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

    public synchronized Transaction updateById(UUID id, TransactionRequest request) {
        int index = findIndexById(id);
        Transaction existing = transactions.get(index);

        Transaction updated = new Transaction(
                existing.id(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                existing.createdAt());

        transactions.set(index, updated);
        return updated;
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


    public synchronized DashboardSummary getDashboardSummary() {
        BalanceSummary summary = getBalanceSummary();
        List<Transaction> recentTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::createdAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return new DashboardSummary(
                transactions.size(),
                summary.income(),
                summary.expenses(),
                summary.balance(),
                recentTransactions);
    }

    public synchronized List<CategorySummary> getCategorySummaries() {
        Map<String, List<Transaction>> byCategory = transactions.stream()
                .collect(LinkedHashMap::new,
                        (map, transaction) -> map.computeIfAbsent(transaction.category(), ignored -> new ArrayList<>()).add(transaction),
                        LinkedHashMap::putAll);

        return byCategory.entrySet().stream()
                .map(entry -> toCategorySummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySummary::category))
                .toList();
    }

    private CategorySummary toCategorySummary(String category, List<Transaction> items) {
        BigDecimal income = items.stream()
                .filter(transaction -> transaction.type() == TransactionType.INCOME)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = items.stream()
                .filter(transaction -> transaction.type() == TransactionType.EXPENSE)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CategorySummary(category, income, expenses, income.subtract(expenses));
    }

    private int findIndexById(UUID id) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new TransactionNotFoundException(id);
    }

    private String normalizeCategory(String category) {
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
