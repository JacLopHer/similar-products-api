package com.company.similarproducts.domain.exception;

import com.company.similarproducts.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ProductNotFoundException.
 * Tests exception creation and message formatting.
 */
@DisplayName("ProductNotFoundException Tests")
class ProductNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with ProductId and correct message")
    void shouldCreateExceptionWithProductIdAndMessage() {
        // Given
        ProductId productId = new ProductId("12345");

        // When
        ProductNotFoundException exception = new ProductNotFoundException(productId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getProductId()).isEqualTo(productId);
        assertThat(exception.getMessage())
                .isEqualTo("Product not found: 12345")
                .contains(productId.value());
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // Given
        ProductId productId = new ProductId("999");

        // When
        ProductNotFoundException exception = new ProductNotFoundException(productId);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should preserve ProductId for retrieval")
    void shouldPreserveProductIdForRetrieval() {
        // Given
        ProductId expectedProductId = new ProductId("ABC-123");

        // When
        ProductNotFoundException exception = new ProductNotFoundException(expectedProductId);

        // Then
        assertThat(exception.getProductId()).isSameAs(expectedProductId);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // Given
        ProductId productId = new ProductId("404");

        // When / Then
        assertThatThrownBy(() -> {
            throw new ProductNotFoundException(productId);
        })
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found")
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should include ProductId in message for different IDs")
    void shouldIncludeProductIdInMessageForDifferentIds() {
        // Given
        ProductId productId1 = new ProductId("1");
        ProductId productId2 = new ProductId("PROD-9999");

        // When
        ProductNotFoundException exception1 = new ProductNotFoundException(productId1);
        ProductNotFoundException exception2 = new ProductNotFoundException(productId2);

        // Then
        assertThat(exception1.getMessage()).contains("1");
        assertThat(exception2.getMessage()).contains("PROD-9999");
    }
}

