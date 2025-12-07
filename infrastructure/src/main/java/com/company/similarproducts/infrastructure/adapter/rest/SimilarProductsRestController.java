package com.company.similarproducts.infrastructure.adapter.rest;

import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.infrastructure.adapter.rest.dto.ProductResponse;
import com.company.similarproducts.infrastructure.adapter.rest.mapper.ProductRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Primary/Driving Adapter - REST Controller.
 * Translates HTTP requests to use case calls.
 */
@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class SimilarProductsRestController {

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;
    private final ProductRestMapper mapper;

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductResponse>> getSimilarProducts(@PathVariable String productId) {
        log.info("REST request received for similar products of productId: {}", productId);
        
        var products = getSimilarProductsUseCase.getSimilarProducts(new ProductId(productId));
        var response = products.stream()
                .map(mapper::toResponse)
                .toList();
        
        log.info("Returning {} similar products", response.size());
        return ResponseEntity.ok(response);
    }
}
