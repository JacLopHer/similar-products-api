package com.company.similarproducts.infrastructure.adapter.http.mapper;

import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert external API DTO to domain Product.
 * Isolates domain from external API structure.
 */
@Component
public class ProductDomainMapper {

    public Product toDomain(ProductApiDto apiDto) {
        return Product.builder()
                .id(apiDto.getId())
                .name(apiDto.getName())
                .price(apiDto.getPrice())
                .availability(apiDto.isAvailability())
                .build();
    }
}
