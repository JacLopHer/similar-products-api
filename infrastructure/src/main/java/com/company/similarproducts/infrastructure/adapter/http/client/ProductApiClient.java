package com.company.similarproducts.infrastructure.adapter.http.client;

import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP Client for external Product API.
 * 100% Reactive with Mono using Reactor .cache() for caching.
 * Cache works correctly with Mono.empty() (products not found).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductApiClient {

    private final WebClient webClient;

    // Manual cache using ConcurrentHashMap for Mono caching
    private final Map<String, Mono<ProductApiDto>> productCache = new ConcurrentHashMap<>();
    private final Map<String, Mono<List<String>>> similarIdsCache = new ConcurrentHashMap<>();

    public Mono<ProductApiDto> getProductById(String productId) {
        if (productId == null || productId.isBlank()) {
            log.warn("getProductById called with null/empty productId");
            return Mono.empty();
        }

        return productCache.computeIfAbsent(productId, id -> {
            log.debug("Cache MISS - Calling external API for product: {}", id);

            return webClient.get()
                    .uri("/product/{productId}", id)
                    .retrieve()
                    .bodyToMono(ProductApiDto.class)
                    .timeout(Duration.ofMillis(2000))
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                        log.debug("Product not found: {}", id);
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.debug("Error loading product {}: {}", id, e.getClass().getSimpleName());
                        return Mono.empty();
                    })
                    .cache(Duration.ofMinutes(10));  // Cache for 10 minutes
        });
    }

    public Mono<List<String>> getSimilarProductIds(String productId) {
        if (productId == null || productId.isBlank()) {
            log.warn("getSimilarProductIds called with null/empty productId");
            return Mono.just(List.of());
        }

        return similarIdsCache.computeIfAbsent(productId, id -> {
            log.debug("Cache MISS - Calling external API for similar products of: {}", id);

            return webClient.get()
                    .uri("/product/{productId}/similarids", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .timeout(Duration.ofMillis(2000))
                    .onErrorResume(e -> {
                        log.debug("Error loading similar products {}: {}", id, e.getClass().getSimpleName());
                        return Mono.just(List.of());
                    })
                    .defaultIfEmpty(List.of())
                    .cache(Duration.ofMinutes(10));  // Cache for 10 minutes
        });
    }
}
