package com.company.similarproducts.infrastructure.adapter.http;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.infrastructure.adapter.http.client.ProductApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("LoadSimilarProductIdsAdapter Tests")

class LoadSimilarProductIdsAdapterTest {
    @Mock
    private ProductApiClient productApiClient;
    private LoadSimilarProductIdsAdapter adapter;
    @BeforeEach
    void setUp() {
        adapter = new LoadSimilarProductIdsAdapter(productApiClient);
    }
    @Test
    @DisplayName("Should load similar product IDs")
    void shouldLoadSimilarProductIds() {
        ProductId productId = new ProductId("1");
        List<String> apiIds = List.of("2", "3", "4");
        when(productApiClient.getSimilarProductIds("1")).thenReturn(apiIds);
        List<ProductId> result = adapter.loadSimilarProductIds(productId);
        assertThat(result).hasSize(3);
        assertThat(result).extracting(ProductId::value)
                .containsExactly("2", "3", "4");
        verify(productApiClient).getSimilarProductIds("1");
    }
    @Test
    @DisplayName("Should return empty list when no similar products")
    void shouldReturnEmptyListWhenNoSimilarProducts() {
        ProductId productId = new ProductId("999");
        when(productApiClient.getSimilarProductIds("999")).thenReturn(List.of());
        List<ProductId> result = adapter.loadSimilarProductIds(productId);
        assertThat(result).isEmpty();
        verify(productApiClient).getSimilarProductIds("999");
    }
}
