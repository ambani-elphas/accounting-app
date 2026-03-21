package com.example.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {
}
