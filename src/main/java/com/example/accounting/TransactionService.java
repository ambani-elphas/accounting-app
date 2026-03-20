package com.example.accounting;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int DASHBOARD_RECENT_LIMIT = 5;
    private static final int DASHBOARD_CATEGORY_LIMIT = 5;

    private final Map<UUID, Transaction> transactionsById = new LinkedHashMap<>();
    private final NavigableSet<Transaction> transactionsByCreatedAt = new TreeSet<>(
            Comparator.comparing(Transaction::createdAt)
                    .reversed()
                    .thenComparing(Transaction::id));
    private final Map<String, CategoryTotals> categoryTotals = new HashMap<>();

    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    public synchronized TransactionPage getAll(int page,
                                               int size,
                                               @Nullable TransactionType type,
                                               @Nullable String category,
                                               @Nullable String description) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedCategory = normalizeOptionalCategory(category);
        String normalizedDescription = normalizeOptionalDescription(description);

        List<Transaction> filtered = transactionsByCreatedAt.stream()
                .filter(transaction -> type == null || transaction.type() == type)
                .filter(transaction -> normalizedCategory == null || transaction.category().equals(normalizedCategory))
                .filter(transaction -> normalizedDescription == null || transaction.description().toLowerCase(Locale.ROOT)
                        .contains(normalizedDescription))
                .toList();

        int fromIndex = Math.min(page * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);

        return new TransactionPage(filtered.subList(fromIndex, toIndex), page, safeSize, filtered.size(), totalPages);
    }

    public synchronized Transaction getById(UUID id) {
        Transaction transaction = transactionsById.get(id);
        if (transaction == null) {
            throw new TransactionNotFoundException(id);
        }
        return transaction;
    }

    public synchronized Transaction create(TransactionRequest request) {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                Instant.now());

        addTransaction(transaction);
        return transaction;
    }

    public synchronized Transaction updateById(UUID id, TransactionRequest request) {
        Transaction existing = getById(id);
        removeTransaction(existing);

        Transaction updated = new Transaction(
                existing.id(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                existing.createdAt());

        addTransaction(updated);
        return updated;
    }

    public synchronized void deleteById(UUID id) {
        Transaction existing = getById(id);
        removeTransaction(existing);
    }

    public synchronized BalanceSummary getBalanceSummary() {
        return new BalanceSummary(totalIncome, totalExpenses, totalIncome.subtract(totalExpenses));
    }

    public synchronized DashboardSummary getDashboardSummary() {
        BalanceSummary summary = getBalanceSummary();
        List<Transaction> recentTransactions = transactionsByCreatedAt.stream()
                .limit(DASHBOARD_RECENT_LIMIT)
                .toList();
        List<CategorySummary> topCategories = getCategorySummaries().stream()
                .sorted(Comparator.comparing(CategorySummary::balance).reversed())
                .limit(DASHBOARD_CATEGORY_LIMIT)
                .toList();

        return new DashboardSummary(
                transactionsById.size(),
                summary.income(),
                summary.expenses(),
                summary.balance(),
                recentTransactions,
                topCategories);
    }

    public synchronized List<CategorySummary> getCategorySummaries() {
        return categoryTotals.entrySet().stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        entry.getValue().income(),
                        entry.getValue().expenses(),
                        entry.getValue().income().subtract(entry.getValue().expenses())))
                .sorted(Comparator.comparing(CategorySummary::category))
                .toList();
    }

    private void addTransaction(Transaction transaction) {
        transactionsById.put(transaction.id(), transaction);
        transactionsByCreatedAt.add(transaction);
        applyToTotals(transaction, BigDecimal::add);
    }

    private void removeTransaction(Transaction transaction) {
        transactionsById.remove(transaction.id());
        transactionsByCreatedAt.remove(transaction);
        applyToTotals(transaction, BigDecimal::subtract);
    }

    private void applyToTotals(Transaction transaction, BigDecimalOperator operator) {
        if (transaction.type() == TransactionType.INCOME) {
            totalIncome = operator.apply(totalIncome, transaction.amount());
        } else {
            totalExpenses = operator.apply(totalExpenses, transaction.amount());
        }

        CategoryTotals totals = categoryTotals.computeIfAbsent(transaction.category(), ignored -> new CategoryTotals());
        totals.apply(transaction.type(), transaction.amount(), operator);

        if (totals.isEmpty()) {
            categoryTotals.remove(transaction.category());
        }
    }

    private String normalizeCategory(String category) {
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String normalizeOptionalCategory(@Nullable String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return normalizeCategory(category);
    }

    private String normalizeOptionalDescription(@Nullable String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim().toLowerCase(Locale.ROOT);
    }

    private interface BigDecimalOperator {
        BigDecimal apply(BigDecimal left, BigDecimal right);
    }

    private static final class CategoryTotals {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expenses = BigDecimal.ZERO;

        void apply(TransactionType type, BigDecimal amount, BigDecimalOperator operator) {
            if (type == TransactionType.INCOME) {
                income = operator.apply(income, amount);
            } else {
                expenses = operator.apply(expenses, amount);
            }
        }

        BigDecimal income() {
            return income;
        }

        BigDecimal expenses() {
            return expenses;
        }

        boolean isEmpty() {
            return income.signum() == 0 && expenses.signum() == 0;
        }
    }
}
