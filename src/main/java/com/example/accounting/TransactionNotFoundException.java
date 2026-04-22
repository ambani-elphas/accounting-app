package com.example.accounting;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {

    private final UUID transactionId;

    public TransactionNotFoundException(UUID id) {
        super("Transaction not found: " + id);
        this.transactionId = id;
    }

    public UUID getTransactionId() {
        return transactionId;
    public TransactionNotFoundException(UUID id) {
        super("Transaction not found: " + id);
    }
}
