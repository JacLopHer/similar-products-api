package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

/**
 * Output port (Secondary/Driven port) - SPI for loading products.
 * Defines what the application needs from infrastructure.
 */
public interface LoadProductPort {
    
    /**
     * Loads a product by its ID.
     *
     * @param productId the product identifier
     * @return Mono containing the product if found, empty Mono otherwise
     */
    Mono<Product> loadProduct(ProductId productId);
}
