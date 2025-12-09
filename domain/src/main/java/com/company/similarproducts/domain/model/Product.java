package com.company.similarproducts.domain.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Product domain entity - Immutable.
 * Pure domain model with business rules.
 */
@Builder
public record Product(String id, String name, BigDecimal price, boolean availability) {
    public ProductId getProductId() {
        return new ProductId(id);
    }
}
