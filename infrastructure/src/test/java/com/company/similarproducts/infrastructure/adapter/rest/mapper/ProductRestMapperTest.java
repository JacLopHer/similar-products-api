package com.company.similarproducts.infrastructure.adapter.rest.mapper;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.infrastructure.adapter.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
@DisplayName("ProductRestMapper Tests")
class ProductRestMapperTest {
    private ProductRestMapper mapper;
    @BeforeEach
    void setUp() {
        mapper = new ProductRestMapper();
    }
    @Test
    @DisplayName("Should map Product to ProductResponse")
    void shouldMapProductToProductResponse() {
        Product product = Product.builder()
                .id("1")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .availability(true)
                .build();
        ProductResponse result = mapper.toResponse(product);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.availability()).isTrue();
    }
    @Test
    @DisplayName("Should map unavailable product")
    void shouldMapUnavailableProduct() {
        Product product = Product.builder()
                .id("2")
                .name("Unavailable")
                .price(BigDecimal.ZERO)
                .availability(false)
                .build();
        ProductResponse result = mapper.toResponse(product);
        assertThat(result.availability()).isFalse();
    }
}
