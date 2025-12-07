package com.company.similarproducts.infrastructure.adapter.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * DTO for product response in REST API.
 * Follows the OpenAPI contract.
 */
@Value
@Builder
public class ProductResponse {
    
    @JsonProperty("id")
    String id;
    
    @JsonProperty("name")
    String name;
    
    @JsonProperty("price")
    BigDecimal price;
    
    @JsonProperty("availability")
    boolean availability;
}
