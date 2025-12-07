package com.company.similarproducts.domain.port;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;

import java.util.List;

/**
 * Input port (Primary/Driving port) - Use Case interface.
 * Defines what the application can do.
 */
public interface GetSimilarProductsUseCase {
    
    /**
     * Gets similar products for a given product ID.
     *
     * @param productId the product identifier
     * @return list of similar products ordered by similarity
     * @throws com.company.similarproducts.domain.exception.ProductNotFoundException if product not found
     */
    List<Product> getSimilarProducts(ProductId productId);
}
