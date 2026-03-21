package com.example.accounting;

import org.springframework.data.jpa.domain.Specification;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<TransactionEntity> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<TransactionEntity> hasCategory(String category) {
        return (root, query, cb) -> category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<TransactionEntity> descriptionContains(String description) {
        return (root, query, cb) -> description == null
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("description")), "%" + description + "%");
    }
}
