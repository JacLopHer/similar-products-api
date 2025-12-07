package com.company.similarproducts.infrastructure.adapter.http.client;

import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * HTTP Client for external Product API.
 * Uses WebClient with Circuit Breaker and Retry patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductApiClient {

    private final WebClient webClient;

    @Value("${external-apis.product-service.timeout:5000}")
    private int timeout;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService")
    public Optional<ProductApiDto> getProductById(String productId) {
        log.debug("Calling external API for product: {}", productId);
        
        try {
            ProductApiDto response = webClient
                    .get()
                    .uri("/product/{productId}", productId)
                    .retrieve()
                    .bodyToMono(ProductApiDto.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
            
            return Optional.ofNullable(response);
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product not found in external API: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling external API for product: {}", productId, e);
            throw e;
        }
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getSimilarProductIdsFallback")
    @Retry(name = "productService")
    public List<String> getSimilarProductIds(String productId) {
        log.debug("Calling external API for similar products of: {}", productId);
        
        try {
            List<String> similarIds = webClient
                    .get()
                    .uri("/product/{productId}/similarids", productId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .timeout(Duration.ofMillis(timeout))
                    .block();
            
            return similarIds != null ? similarIds : List.of();
            
        } catch (Exception e) {
            log.error("Error calling external API for similar products: {}", productId, e);
            throw e;
        }
    }

    // Fallback methods
    private Optional<ProductApiDto> getProductByIdFallback(String productId, Exception ex) {
        log.warn("Circuit breaker activated for product: {}. Returning empty.", productId);
        return Optional.empty();
    }

    private List<String> getSimilarProductIdsFallback(String productId, Exception ex) {
        log.warn("Circuit breaker activated for similar products: {}. Returning empty list.", productId);
        return List.of();
    }
}
