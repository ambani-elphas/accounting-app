package com.example.accounting;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "{validation.description.required}") String description,
        @NotBlank(message = "{validation.category.required}") String category,
        @NotNull(message = "{validation.amount.required}")
        @DecimalMin(value = "0.01", message = "{validation.amount.positive}") BigDecimal amount,
        @NotNull(message = "{validation.type.required}") TransactionType type
        @NotBlank(message = "description is required") String description,
        @NotBlank(message = "category is required") String category,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount,
        @NotNull(message = "type is required") TransactionType type
) {
}
