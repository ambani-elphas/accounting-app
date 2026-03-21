package com.example.accounting;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int DASHBOARD_RECENT_LIMIT = 5;
    private static final int DASHBOARD_CATEGORY_LIMIT = 5;

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionPage getAll(int page,
                                  int size,
                                  @Nullable TransactionType type,
                                  @Nullable String category,
                                  @Nullable String description) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedCategory = normalizeOptionalCategory(category);
        String normalizedDescription = normalizeOptionalDescription(description);

        Specification<TransactionEntity> specification = Specification
                .where(TransactionSpecifications.hasType(type))
                .and(TransactionSpecifications.hasCategory(normalizedCategory))
                .and(TransactionSpecifications.descriptionContains(normalizedDescription));

        var resultPage = transactionRepository.findAll(
                specification,
                PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        return new TransactionPage(
                resultPage.getContent().stream().map(this::toTransaction).toList(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages());
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .map(this::toTransaction)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public Transaction create(TransactionRequest request) {
        TransactionEntity transaction = new TransactionEntity(
                UUID.randomUUID(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                Instant.now());

        return toTransaction(transactionRepository.save(transaction));
    }

    public Transaction updateById(UUID id, TransactionRequest request) {
        TransactionEntity existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        TransactionEntity updated = new TransactionEntity(
                existing.getId(),
                request.description().trim(),
                normalizeCategory(request.category()),
                request.amount(),
                request.type(),
                existing.getCreatedAt());

        return toTransaction(transactionRepository.save(updated));
    }

    public void deleteById(UUID id) {
        TransactionEntity existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transactionRepository.delete(existing);
    }

    public BalanceSummary getBalanceSummary() {
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .map(this::toTransaction)
                .toList();
        return summarize(transactions);
    }

    public DashboardSummary getDashboardSummary() {
        List<Transaction> allTransactions = transactionRepository.findAll().stream()
                .map(this::toTransaction)
                .toList();
        BalanceSummary summary = summarize(allTransactions);

        List<Transaction> recentTransactions = transactionRepository.findAll(
                        PageRequest.of(0, DASHBOARD_RECENT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent()
                .stream()
                .map(this::toTransaction)
                .toList();

        List<CategorySummary> topCategories = getCategorySummaries().stream()
                .sorted(Comparator.comparing(CategorySummary::balance).reversed())
                .limit(DASHBOARD_CATEGORY_LIMIT)
                .toList();

        return new DashboardSummary(
                transactionRepository.count(),
                summary.income(),
                summary.expenses(),
                summary.balance(),
                recentTransactions,
                topCategories);
    }

    public List<CategorySummary> getCategorySummaries() {
        return transactionRepository.findAll().stream()
                .map(this::toTransaction)
                .collect(java.util.stream.Collectors.groupingBy(Transaction::category))
                .entrySet().stream()
                .map(entry -> summarizeCategory(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySummary::category))
                .toList();
    }

    private BalanceSummary summarize(List<Transaction> transactions) {
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

    private CategorySummary summarizeCategory(String category, List<Transaction> transactions) {
        BalanceSummary summary = summarize(transactions);
        return new CategorySummary(category, summary.income(), summary.expenses(), summary.balance());
    }

    private Transaction toTransaction(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getType(),
                entity.getCreatedAt());
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
}
