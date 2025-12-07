package com.company.similarproducts.infrastructure.adapter.http.client;

import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
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
 * 100% Reactive with Mono.cache() - Simple and effective.
 * NO CompletableFuture, NO AsyncCache mixing.
 */
@Slf4j
@Component
public class ProductApiClient {

    private final WebClient webClient;

    // Simple ConcurrentHashMap to store cached Mono pipelines
    private final Map<String, Mono<ProductApiDto>> productCache = new ConcurrentHashMap<>();
    private final Map<String, Mono<List<String>>> similarIdsCache = new ConcurrentHashMap<>();

    public ProductApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ProductApiDto> getProductById(String productId) {
        if (productId == null || productId.isBlank()) {
            return Mono.empty();
        }

        return productCache.computeIfAbsent(productId, id ->
            webClient.get()
                    .uri("/product/{productId}", id)
                    .retrieve()
                    .bodyToMono(ProductApiDto.class)
                    .timeout(Duration.ofMillis(2000))
                    .doOnSubscribe(s -> log.debug("Cache MISS - Calling external API for product: {}", id))
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                        log.debug("Product not found: {}", id);
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.debug("Error loading product {}: {}", id, e.getClass().getSimpleName());
                        return Mono.empty();
                    })
                    .cache(Duration.ofMinutes(10))
        );
    }

    public Mono<List<String>> getSimilarProductIds(String productId) {
        if (productId == null || productId.isBlank()) {
            return Mono.just(List.of());
        }

        return similarIdsCache.computeIfAbsent(productId, id ->
            webClient.get()
                    .uri("/product/{productId}/similarids", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .timeout(Duration.ofMillis(2000))
                    .doOnSubscribe(s -> log.debug("Cache MISS - Calling external API for similar IDs: {}", id))
                    .onErrorResume(e -> {
                        log.debug("Error loading similar IDs {}: {}", id, e.getClass().getSimpleName());
                        return Mono.just(List.of());
                    })
                    .defaultIfEmpty(List.of())
                    .cache(Duration.ofMinutes(10))
        );
    }
}
