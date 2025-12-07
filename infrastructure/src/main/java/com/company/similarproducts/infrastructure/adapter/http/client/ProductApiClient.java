package com.company.similarproducts.infrastructure.adapter.http.client;

import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.company.similarproducts.infrastructure.config.CacheConfig.PRODUCTS_CACHE;
import static com.company.similarproducts.infrastructure.config.CacheConfig.SIMILAR_IDS_CACHE;

/**
 * HTTP Client for external Product API.
 * Uses WebClient with Circuit Breaker, Retry and Cache patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductApiClient {

    private final WebClient webClient;

    @Value("${external-apis.product-service.timeout:5000}")
    private int timeout;

    @Cacheable(value = PRODUCTS_CACHE, key = "#productId", unless = "#result == null")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService")
    public Mono<ProductApiDto> getProductById(String productId) {
        log.debug("Calling external API for product: {} (cache miss)", productId);

        return webClient
                .get()
                .uri("/product/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductApiDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.warn("Product not found in external API: {}", productId);
                    return Mono.empty();
                })
                .doOnError(e -> {
                    if (!(e instanceof WebClientResponseException.NotFound)) {
                        log.error("Error calling external API for product {}: {}",
                            productId, e.getClass().getSimpleName());
                    }
                });
    }

    @Cacheable(value = SIMILAR_IDS_CACHE, key = "#productId")
    @CircuitBreaker(name = "productService", fallbackMethod = "getSimilarProductIdsFallback")
    @Retry(name = "productService")
    public Mono<List<String>> getSimilarProductIds(String productId) {
        log.debug("Calling external API for similar products of: {} (cache miss)", productId);

        return webClient
                .get()
                .uri("/product/{productId}/similarids", productId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .timeout(Duration.ofMillis(timeout))
                .defaultIfEmpty(List.of())
                .doOnError(e -> log.error("Error calling external API for similar products {}: {}",
                    productId, e.getClass().getSimpleName()));
    }

    private Mono<ProductApiDto> getProductByIdFallback(String productId, Exception ex) {
        if (ex.getClass().getSimpleName().contains("CallNotPermitted")) {
            log.debug("Circuit OPEN - Skipping call to product: {}", productId);
        } else if (ex instanceof TimeoutException) {
            log.warn("Timeout calling product {}: API took >{}ms", productId, timeout);
        } else {
            log.warn("Fallback for product {}: {}", productId, ex.getClass().getSimpleName());
        }
        return Mono.empty();
    }

    private Mono<List<String>> getSimilarProductIdsFallback(String productId, Exception ex) {
        if (ex.getClass().getSimpleName().contains("CallNotPermitted")) {
            log.debug("Circuit OPEN - Skipping call to similar products: {}", productId);
        } else if (ex instanceof TimeoutException) {
            log.warn("Timeout calling similar products {}: API took >{}ms", productId, timeout);
        } else {
            log.warn("Fallback for similar products {}: {}", productId, ex.getClass().getSimpleName());
        }
        return Mono.just(List.of());
    }
}
