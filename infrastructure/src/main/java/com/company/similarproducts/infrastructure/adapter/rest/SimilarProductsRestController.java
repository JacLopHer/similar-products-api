package com.company.similarproducts.infrastructure.adapter.rest;

import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.infrastructure.adapter.rest.dto.ProductResponse;
import com.company.similarproducts.infrastructure.adapter.rest.mapper.ProductRestMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Primary/Driving Adapter - REST Controller.
 * 100% reactive with Mono for WebFlux.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class SimilarProductsRestController {

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;
    private final ProductRestMapper mapper;

    @GetMapping("/{productId}/similar")
    public Mono<ResponseEntity<List<ProductResponse>>> getSimilarProducts(
            @PathVariable("productId") @NotBlank(message = "Product ID must not be blank") String productId) {
        log.info("REST request received for similar products of productId: {}", productId);
        
        return getSimilarProductsUseCase.getSimilarProducts(new ProductId(productId))
                .map(products -> products.stream()
                        .map(mapper::toResponse)
                        .toList())
                .doOnSuccess(response -> log.info("Returning {} similar products", response.size()))
                .map(ResponseEntity::ok);
    }
}
