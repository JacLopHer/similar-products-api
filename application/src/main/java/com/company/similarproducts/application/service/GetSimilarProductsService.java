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
                .flatMap(product -> {
                    log.debug("Product {} found, loading similar IDs", productId);
                    return loadSimilarProductIdsPort.loadSimilarProductIds(productId);
                })
                .flatMapMany(Flux::fromIterable)
                .filter(id -> id != null && id.value() != null && !id.value().isBlank())
                .flatMap(
                        id -> loadProductPort.loadProduct(id)
                                .onErrorResume(e -> {
                                    log.debug("Failed to load product {}: {}", id, e.getMessage());
                                    return Mono.empty();
                                }),
                        8
                )
                .collectList()
                .doOnSuccess(products -> log.info("Returning {} similar products", products.size()));
    }
}
