package com.company.similarproducts.infrastructure.adapter.http;

import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.LoadSimilarProductIdsPort;
import com.company.similarproducts.infrastructure.adapter.http.client.ProductApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Secondary/Driven Adapter - Implements LoadSimilarProductIdsPort.
 * Calls external similar products API via HTTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoadSimilarProductIdsAdapter implements LoadSimilarProductIdsPort {

    private final ProductApiClient productApiClient;

    @Override
    public Mono<List<ProductId>> loadSimilarProductIds(ProductId productId) {
        log.debug("Loading similar product IDs via HTTP: {}", productId);
        
        return productApiClient.getSimilarProductIds(productId.value())
                .map(ids -> ids.stream()
                        .filter(id -> id != null && !id.isBlank())
                        .map(ProductId::new)
                        .toList());
    }
}
