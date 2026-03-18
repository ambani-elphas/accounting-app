package com.example.accounting;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public TransactionPage getAll(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "25") int size,
                                  @RequestParam(required = false) TransactionType type,
                                  @RequestParam(required = false) String category) {
        return transactionService.getAll(page, size, type, category);
    }

    @GetMapping("/{id}")
    public Transaction getById(@PathVariable UUID id) {
        return transactionService.getById(id);
    }

    @GetMapping("/summary")
    public BalanceSummary getSummary() {
        return transactionService.getBalanceSummary();
    }

    @GetMapping("/summary/by-category")
    public List<CategorySummary> getCategorySummary() {
        return transactionService.getCategorySummaries();
    }

    @GetMapping("/dashboard")
    public DashboardSummary getDashboard() {
        return transactionService.getDashboardSummary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.create(request);
    }

    @PutMapping("/{id}")
    public Transaction update(@PathVariable UUID id, @Valid @RequestBody TransactionRequest request) {
        return transactionService.updateById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        transactionService.deleteById(id);
    }
}
