package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Output port (Secondary/Driven port) - SPI for loading similar product IDs.
 * Defines what the application needs from infrastructure.
 * REACTIVE - uses Mono for non-blocking I/O.
 */
public interface LoadSimilarProductIdsPort {
    
    /**
     * Loads the IDs of products similar to the given product - REACTIVE.
     *
     * @param productId the product identifier
     * @return Mono with list of similar product IDs ordered by similarity
     */
    Mono<List<ProductId>> loadSimilarProductIds(ProductId productId);
}
