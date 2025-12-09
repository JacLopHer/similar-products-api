package com.company.similarproducts.infrastructure.adapter.http.mapper;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductDomainMapper Tests")
class ProductDomainMapperTest {
    private ProductDomainMapper mapper;
    @BeforeEach
    void setUp() {
        mapper = new ProductDomainMapper();
    }
    @Test
    @DisplayName("Should map ProductApiDto to Product domain entity")
    void shouldMapProductApiDtoToProduct() {
        ProductApiDto apiDto = ProductApiDto.builder()
                .id("1")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .availability(true)
                .build();
        Product result = mapper.toDomain(apiDto);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.availability()).isTrue();
    }
    @Test
    @DisplayName("Should map unavailable product")
    void shouldMapUnavailableProduct() {
        ProductApiDto apiDto = ProductApiDto.builder()
                .id("2")
                .name("Unavailable")
                .price(BigDecimal.ZERO)
                .availability(false)
                .build();
        Product result = mapper.toDomain(apiDto);
        assertThat(result.availability()).isFalse();
    }
}
