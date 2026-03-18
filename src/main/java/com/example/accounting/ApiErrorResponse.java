package com.example.accounting;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<String> errors
) {
}
