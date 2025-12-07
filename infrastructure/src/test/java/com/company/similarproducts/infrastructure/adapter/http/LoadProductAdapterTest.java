package com.company.similarproducts.infrastructure.adapter.http;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.infrastructure.adapter.http.client.ProductApiClient;
import com.company.similarproducts.infrastructure.adapter.http.dto.ProductApiDto;
import com.company.similarproducts.infrastructure.adapter.http.mapper.ProductDomainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("LoadProductAdapter Tests")

class LoadProductAdapterTest {
    @Mock
    private ProductApiClient productApiClient;
    @Mock
    private ProductDomainMapper mapper;
    private LoadProductAdapter adapter;
    @BeforeEach
    void setUp() {
        adapter = new LoadProductAdapter(productApiClient, mapper);
    }
    @Test
    @DisplayName("Should load product when found in API")
    void shouldLoadProductWhenFound() {
        ProductId productId = new ProductId("1");
        ProductApiDto apiDto = ProductApiDto.builder()
                .id("1")
                .name("Product")
                .price(BigDecimal.TEN)
                .availability(true)
                .build();
        Product expectedProduct = Product.builder()
                .id("1")
                .name("Product")
                .price(BigDecimal.TEN)
                .availability(true)
                .build();
        when(productApiClient.getProductById("1")).thenReturn(Optional.of(apiDto));
        when(mapper.toDomain(apiDto)).thenReturn(expectedProduct);
        Optional<Product> result = adapter.loadProduct(productId);
        assertThat(result).isPresent().contains(expectedProduct);
        verify(productApiClient).getProductById("1");
        verify(mapper).toDomain(apiDto);
    }
    @Test
    @DisplayName("Should return empty when product not found")
    void shouldReturnEmptyWhenNotFound() {
        ProductId productId = new ProductId("999");
        when(productApiClient.getProductById("999")).thenReturn(Optional.empty());
        Optional<Product> result = adapter.loadProduct(productId);
        assertThat(result).isEmpty();
        verify(productApiClient).getProductById("999");
        verify(mapper, never()).toDomain(any());
    }
}
