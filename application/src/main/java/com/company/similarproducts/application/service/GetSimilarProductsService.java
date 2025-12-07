package com.company.similarproducts.application.service;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.domain.port.LoadProductPort;
import com.company.similarproducts.domain.port.LoadSimilarProductIdsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Application Service implementing the GetSimilarProductsUseCase.
 * 100% reactive with Mono + Flux for high concurrency.
 * Uses flatMap with concurrency for parallel product loading.
 */
@Slf4j
@RequiredArgsConstructor
public class GetSimilarProductsService implements GetSimilarProductsUseCase {

    private final LoadProductPort loadProductPort;
    private final LoadSimilarProductIdsPort loadSimilarProductIdsPort;

    @Override
    public Mono<List<Product>> getSimilarProducts(ProductId productId) {
        log.info("Getting similar products for: {}", productId);

        return loadProductPort.loadProduct(productId)
                .flatMap(product -> {
                    log.debug("Product {} found, loading similar IDs", productId);
                    return loadSimilarProductIdsPort.loadSimilarProductIds(productId);
                })
                .flatMapMany(Flux::fromIterable)
                .flatMap(
                        id -> loadProductPort.loadProduct(id)
                                .doOnError(e -> log.debug("Failed to load product {}: {}", id, e.getMessage()))
                                .onErrorResume(e -> Mono.empty()),
                        256
                )
                .collectList()
                .doOnSuccess(products -> log.info("Returning {} similar products", products.size()))
                .defaultIfEmpty(List.of());
    }
}
