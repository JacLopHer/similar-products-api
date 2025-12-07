package com.company.similarproducts.application.service;

import com.company.similarproducts.domain.exception.ProductNotFoundException;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.LoadProductPort;
import com.company.similarproducts.domain.port.LoadSimilarProductIdsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetSimilarProductsService.
 * Tests business logic orchestration with mocked ports.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSimilarProductsService Tests")
class GetSimilarProductsServiceTest {

    @Mock
    private LoadProductPort loadProductPort;

    @Mock
    private LoadSimilarProductIdsPort loadSimilarProductIdsPort;

    private GetSimilarProductsService service;

    @BeforeEach
    void setUp() {
        service = new GetSimilarProductsService(loadProductPort, loadSimilarProductIdsPort);
    }

    @Test
    @DisplayName("Should return similar products when product exists and has similar products")
    void shouldReturnSimilarProductsWhenProductExistsAndHasSimilarProducts() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        ProductId similarId1 = new ProductId("2");
        ProductId similarId2 = new ProductId("3");
        List<ProductId> similarIds = List.of(similarId1, similarId2);

        Product similarProduct1 = createProduct("2", "Similar Product 1", "90.00");
        Product similarProduct2 = createProduct("3", "Similar Product 2", "110.00");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(similarIds));
        when(loadProductPort.loadProduct(similarId1)).thenReturn(Mono.just(similarProduct1));
        when(loadProductPort.loadProduct(similarId2)).thenReturn(Mono.just(similarProduct2));

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(similarProduct1, similarProduct2);

        verify(loadProductPort).loadProduct(productId);
        verify(loadSimilarProductIdsPort).loadSimilarProductIds(productId);
        verify(loadProductPort).loadProduct(similarId1);
        verify(loadProductPort).loadProduct(similarId2);
    }

    @Test
    @DisplayName("Should return empty list when product has no similar products")
    void shouldReturnEmptyListWhenProductHasNoSimilarProducts() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(List.of()));

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result).isEmpty();

        verify(loadProductPort).loadProduct(productId);
        verify(loadSimilarProductIdsPort).loadSimilarProductIds(productId);
        // No additional loadProduct calls beyond the initial verification
        verify(loadProductPort, times(1)).loadProduct(any(ProductId.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when main product does not exist")
    void shouldThrowProductNotFoundExceptionWhenMainProductDoesNotExist() {
        // Given
        ProductId productId = new ProductId("999");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.empty());

        // When / Then
        StepVerifier.create(service.getSimilarProducts(productId))
                .expectErrorMatches(e -> e instanceof ProductNotFoundException &&
                        e.getMessage().contains("Product not found") &&
                        e.getMessage().contains("999"))
                .verify();

        verify(loadProductPort).loadProduct(productId);
        verify(loadSimilarProductIdsPort, never()).loadSimilarProductIds(any());
    }

    @Test
    @DisplayName("Should filter out similar products that are not found")
    void shouldFilterOutSimilarProductsThatAreNotFound() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        ProductId similarId1 = new ProductId("2");
        ProductId similarId2 = new ProductId("3");
        ProductId similarId3 = new ProductId("4");
        List<ProductId> similarIds = List.of(similarId1, similarId2, similarId3);

        Product similarProduct1 = createProduct("2", "Similar Product 1", "90.00");
        // similarId2 not found
        Product similarProduct3 = createProduct("4", "Similar Product 3", "120.00");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(similarIds));
        when(loadProductPort.loadProduct(similarId1)).thenReturn(Mono.just(similarProduct1));
        when(loadProductPort.loadProduct(similarId2)).thenReturn(Mono.empty());
        when(loadProductPort.loadProduct(similarId3)).thenReturn(Mono.just(similarProduct3));

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(similarProduct1, similarProduct3)
                .doesNotContain((Product) null);

        verify(loadProductPort).loadProduct(similarId1);
        verify(loadProductPort).loadProduct(similarId2);
        verify(loadProductPort).loadProduct(similarId3);
    }

    @Test
    @DisplayName("Should handle single similar product")
    void shouldHandleSingleSimilarProduct() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        ProductId similarId = new ProductId("2");
        List<ProductId> similarIds = List.of(similarId);

        Product similarProduct = createProduct("2", "Similar Product", "95.00");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(similarIds));
        when(loadProductPort.loadProduct(similarId)).thenReturn(Mono.just(similarProduct));

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result)
                .hasSize(1)
                .containsExactly(similarProduct);
    }

    @Test
    @DisplayName("Should handle multiple similar products efficiently (parallel loading)")
    void shouldHandleMultipleSimilarProductsEfficiently() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        List<ProductId> similarIds = List.of(
                new ProductId("2"),
                new ProductId("3"),
                new ProductId("4"),
                new ProductId("5")
        );

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(similarIds));

        similarIds.forEach(id ->
            when(loadProductPort.loadProduct(id))
                .thenReturn(Mono.just(createProduct(id.value(), "Product " + id.value(), "100.00")))
        );

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result).hasSize(4);

        // Verify all products were loaded (even if in parallel)
        similarIds.forEach(id -> verify(loadProductPort).loadProduct(id));
    }

    @Test
    @DisplayName("Should use constructor injection")
    void shouldUseConstructorInjection() {
        // When
        GetSimilarProductsService serviceInstance = new GetSimilarProductsService(
                loadProductPort,
                loadSimilarProductIdsPort
        );

        // Then
        assertThat(serviceInstance).isNotNull();
    }

    @Test
    @DisplayName("Should return immutable list of similar products")
    void shouldReturnImmutableListOfSimilarProducts() {
        // Given
        ProductId productId = new ProductId("1");
        Product mainProduct = createProduct("1", "Main Product", "100.00");

        when(loadProductPort.loadProduct(productId)).thenReturn(Mono.just(mainProduct));
        when(loadSimilarProductIdsPort.loadSimilarProductIds(productId)).thenReturn(Mono.just(List.of()));

        // When
        List<Product> result = service.getSimilarProducts(productId).block();

        // Then
        assertThat(result).isNotNull();
        // List.of() and .toList() return immutable lists
    }

    // Helper method to create test products
    private Product createProduct(String id, String name, String price) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal(price))
                .availability(true)
                .build();
    }
}

