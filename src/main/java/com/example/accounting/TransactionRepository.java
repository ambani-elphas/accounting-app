package com.example.accounting;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    @Query("""
            select
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.INCOME then t.amount else 0 end), 0) as income,
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.EXPENSE then t.amount else 0 end), 0) as expenses
            from TransactionEntity t
            """)
    BalanceAggregateRow summarizeTotals();

    @Query("""
            select
                t.category as category,
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.INCOME then t.amount else 0 end), 0) as income,
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.EXPENSE then t.amount else 0 end), 0) as expenses
            from TransactionEntity t
            group by t.category
            """)
    List<CategoryAggregateRow> summarizeByCategory();

    @Query("""
            select
                t.category as category,
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.INCOME then t.amount else 0 end), 0) as income,
                coalesce(sum(case when t.type = com.example.accounting.TransactionType.EXPENSE then t.amount else 0 end), 0) as expenses
            from TransactionEntity t
            group by t.category
            order by (coalesce(sum(case when t.type = com.example.accounting.TransactionType.INCOME then t.amount else 0 end), 0)
                     - coalesce(sum(case when t.type = com.example.accounting.TransactionType.EXPENSE then t.amount else 0 end), 0)) desc
            """)
    List<CategoryAggregateRow> summarizeTopCategories(Pageable pageable);
}
