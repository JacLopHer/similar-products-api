package com.company.similarproducts.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ProductId Value Object.
 * Tests validation rules and behavior.
 */
@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

    @Test
    @DisplayName("Should create ProductId with valid value")
    void shouldCreateProductIdWithValidValue() {
        // Given
        String validId = "123";

        // When
        ProductId productId = new ProductId(validId);

        // Then
        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(validId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Should throw exception when ProductId is null, empty or blank")
    void shouldThrowExceptionWhenProductIdIsInvalid(String invalidId) {
        // When / Then
        assertThatThrownBy(() -> new ProductId(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should return value as string representation")
    void shouldReturnValueAsStringRepresentation() {
        // Given
        String id = "12345";
        ProductId productId = new ProductId(id);

        // When
        String result = productId.toString();

        // Then
        assertThat(result).isEqualTo(id);
    }

    @Test
    @DisplayName("Should be equal when same value")
    void shouldBeEqualWhenSameValue() {
        // Given
        ProductId productId1 = new ProductId("123");
        ProductId productId2 = new ProductId("123");

        // Then
        assertThat(productId1).isEqualTo(productId2)
                .hasSameHashCodeAs(productId2);
    }

    @Test
    @DisplayName("Should not be equal when different value")
    void shouldNotBeEqualWhenDifferentValue() {
        // Given
        ProductId productId1 = new ProductId("123");
        ProductId productId2 = new ProductId("456");

        // Then
        assertThat(productId1).isNotEqualTo(productId2);
    }

    @Test
    @DisplayName("Should handle different ID formats")
    void shouldHandleDifferentIdFormats() {
        // Given / When / Then
        assertThatNoException().isThrownBy(() -> {
            new ProductId("1");
            new ProductId("abc123");
            new ProductId("PROD-12345");
            new ProductId("123-456-789");
        });
    }
}


