package com.company.similarproducts.application.service;

import com.company.similarproducts.domain.exception.ProductNotFoundException;
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
 * Orchestrates the business logic using domain ports.
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
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(product -> loadSimilarProductIdsPort.loadSimilarProductIds(productId))
                .doOnNext(ids -> log.debug("Found {} similar product IDs", ids.size()))
                .flatMapMany(Flux::fromIterable)
                .filter(id -> id != null && id.value() != null && !id.value().isBlank())  // ⚡ AGREGAR ESTO
                .flatMap(id -> loadProductPort.loadProduct(id)
                        .onErrorResume(e -> {
                            log.warn("Failed to load product {}: {}", id, e.getMessage());
                            return Mono.empty();
                        })
                )
                .collectList()
                .doOnSuccess(products -> log.info("Returning {} similar products", products.size()));
    }
}
