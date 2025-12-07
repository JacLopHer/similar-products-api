package com.company.similarproducts.domain.exception;

import com.company.similarproducts.domain.model.ProductId;

/**
 * Domain exception thrown when a product is not found.
 */
public class ProductNotFoundException extends RuntimeException {
    
    private final ProductId productId;

    public ProductNotFoundException(ProductId productId) {
        super("Product not found: " + productId);
        this.productId = productId;
    }

    public ProductId getProductId() {
        return productId;
    }
}
