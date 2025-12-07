package com.company.similarproducts.infrastructure.adapter.rest.mapper;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.infrastructure.adapter.rest.dto.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert domain Product to REST ProductResponse.
 * Isolates domain from REST concerns.
 */
@Component
public class ProductRestMapper {

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .availability(product.isAvailability())
                .build();
    }
}
