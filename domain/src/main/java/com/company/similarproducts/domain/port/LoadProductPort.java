package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

/**
 * Output port (Secondary/Driven port) - SPI for loading products.
 * Uses Reactor Mono for reactive non-blocking I/O.
 */
public interface LoadProductPort {
    
    /**
     * Loads a product by its ID reactively.
     *
     * @param productId the product identifier
     * @return Mono containing the product if found, empty Mono otherwise
     */
    Mono<Product> loadProduct(ProductId productId);
}
