package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

/**
 * Output port (Secondary/Driven port) - SPI for loading products.
 * Defines what the application needs from infrastructure.
 * REACTIVE - uses Mono for non-blocking I/O.
 */
public interface LoadProductPort {
    
    /**
     * Loads a product by its ID - REACTIVE.
     *
     * @param productId the product identifier
     * @return Mono containing the product if found, empty Mono otherwise
     */
    Mono<Product> loadProduct(ProductId productId);
}
