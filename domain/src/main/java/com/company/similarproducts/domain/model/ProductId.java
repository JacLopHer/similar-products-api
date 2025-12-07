package com.company.similarproducts.domain.model;

/**
 * Value Object representing a Product ID.
 * Ensures type safety and validation.
 */
public record ProductId(String value) {
    public ProductId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
