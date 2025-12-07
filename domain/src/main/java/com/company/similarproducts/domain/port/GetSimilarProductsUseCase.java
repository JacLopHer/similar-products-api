package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Input port (Primary/Driving port) - Use Case interface.
 * Uses Reactor Mono for reactive non-blocking execution.
 */
public interface GetSimilarProductsUseCase {
    
    /**
     * Gets similar products for a given product ID reactively.
     *
     * @param productId the product identifier
     * @return Mono with list of similar products ordered by similarity
     */
    Mono<List<Product>> getSimilarProducts(ProductId productId);
}
