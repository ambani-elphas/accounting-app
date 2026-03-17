package com.example.accounting;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final List<Transaction> transactions = new ArrayList<>();

    @GetMapping
    public List<Transaction> getAll() {
        return transactions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                request.description(),
                request.amount(),
                request.type(),
                Instant.now());
        transactions.add(transaction);
        return transaction;
    }
}
