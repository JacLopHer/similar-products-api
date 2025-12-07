package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.ProductId;

import java.util.List;

/**
 * Output port (Secondary/Driven port) - SPI for loading similar product IDs.
 * Defines what the application needs from infrastructure.
 */
public interface LoadSimilarProductIdsPort {
    
    /**
     * Loads the IDs of products similar to the given product.
     *
     * @param productId the product identifier
     * @return list of similar product IDs ordered by similarity
     */
    List<ProductId> loadSimilarProductIds(ProductId productId);
}
