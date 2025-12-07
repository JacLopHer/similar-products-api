package com.company.similarproducts.application.service;

import com.company.similarproducts.domain.exception.ProductNotFoundException;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.domain.port.LoadProductPort;
import com.company.similarproducts.domain.port.LoadSimilarProductIdsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Application Service implementing the GetSimilarProductsUseCase.
 * Orchestrates the business logic using domain ports.
 * NO infrastructure dependencies - only domain interfaces.
 */
@Slf4j
@RequiredArgsConstructor
public class GetSimilarProductsService implements GetSimilarProductsUseCase {

    private final LoadProductPort loadProductPort;
    private final LoadSimilarProductIdsPort loadSimilarProductIdsPort;

    @Override
    public List<Product> getSimilarProducts(ProductId productId) {
        log.info("Getting similar products for: {}", productId);
        
        // Verify the product exists
        loadProductPort.loadProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Get similar product IDs
        List<ProductId> similarProductIds = loadSimilarProductIdsPort.loadSimilarProductIds(productId);
        
        log.debug("Found {} similar product IDs", similarProductIds.size());

        // Load all similar products in parallel for performance
        List<CompletableFuture<Product>> futures = similarProductIds.stream()
                .map(this::loadProductAsync)
                .toList();

        // Wait for all futures and collect results, filtering out not found products
        List<Product> similarProducts = futures.stream()
                .map(CompletableFuture::join)
                .filter(product -> product != null)
                .toList();
        
        log.info("Returning {} similar products", similarProducts.size());
        return similarProducts;
    }

    private CompletableFuture<Product> loadProductAsync(ProductId productId) {
        return CompletableFuture.supplyAsync(() -> 
            loadProductPort.loadProduct(productId).orElse(null)
        );
    }
}
