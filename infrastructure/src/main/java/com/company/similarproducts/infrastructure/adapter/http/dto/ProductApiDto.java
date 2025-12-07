package com.company.similarproducts.infrastructure.adapter.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for external Product API response.
 * Matches the structure of the external API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductApiDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("availability")
    private boolean availability;
}
