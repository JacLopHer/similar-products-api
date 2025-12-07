package com.company.similarproducts.infrastructure.adapter.http;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.LoadProductPort;
import com.company.similarproducts.infrastructure.adapter.http.client.ProductApiClient;
import com.company.similarproducts.infrastructure.adapter.http.mapper.ProductDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Secondary/Driven Adapter - Implements LoadProductPort.
 * Calls external product API via HTTP.
 * REACTIVE - returns Mono for non-blocking I/O.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoadProductAdapter implements LoadProductPort {

    private final ProductApiClient productApiClient;
    private final ProductDomainMapper mapper;

    @Override
    public Mono<Product> loadProduct(ProductId productId) {
        log.debug("Loading product via HTTP: {}", productId);
        
        return productApiClient.getProductById(productId.value())
                .map(mapper::toDomain);
    }
}
