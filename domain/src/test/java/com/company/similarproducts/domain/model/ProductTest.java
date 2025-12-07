package com.company.similarproducts.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Product domain entity.
 * Tests immutability and behavior.
 */
@DisplayName("Product Domain Entity Tests")
class ProductTest {

    @Test
    @DisplayName("Should create Product with all fields using builder")
    void shouldCreateProductWithAllFields() {
        // Given
        String id = "1";
        String name = "Test Product";
        BigDecimal price = new BigDecimal("99.99");
        boolean availability = true;

        // When
        Product product = Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .availability(availability)
                .build();

        // Then
        assertThat(product).isNotNull();
        assertThat(product.id()).isEqualTo(id);
        assertThat(product.name()).isEqualTo(name);
        assertThat(product.price()).isEqualByComparingTo(price);
        assertThat(product.availability()).isEqualTo(availability);
    }

    @Test
    @DisplayName("Should return ProductId from id field")
    void shouldReturnProductIdFromIdField() {
        // Given
        String id = "12345";
        Product product = Product.builder()
                .id(id)
                .name("Product")
                .price(BigDecimal.TEN)
                .availability(true)
                .build();

        // When
        ProductId productId = product.getProductId();

        // Then
        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should be immutable - using Lombok @Value")
    void shouldBeImmutable() {
        // Given
        Product product = Product.builder()
                .id("1")
                .name("Original")
                .price(new BigDecimal("10.00"))
                .availability(true)
                .build();

        // Then - no setters should exist
        assertThat(product.getClass().getMethods())
                .noneMatch(method -> method.getName().startsWith("set"));
    }

    @Test
    @DisplayName("Should create Product with unavailable status")
    void shouldCreateProductWithUnavailableStatus() {
        // When
        Product product = Product.builder()
                .id("1")
                .name("Unavailable Product")
                .price(BigDecimal.ZERO)
                .availability(false)
                .build();

        // Then
        assertThat(product.availability()).isFalse();
    }

    @Test
    @DisplayName("Should handle large prices")
    void shouldHandleLargePrices() {
        // Given
        BigDecimal largePrice = new BigDecimal("999999.99");

        // When
        Product product = Product.builder()
                .id("1")
                .name("Expensive Product")
                .price(largePrice)
                .availability(true)
                .build();

        // Then
        assertThat(product.price()).isEqualByComparingTo(largePrice);
    }

    @Test
    @DisplayName("Should handle zero price")
    void shouldHandleZeroPrice() {
        // When
        Product product = Product.builder()
                .id("1")
                .name("Free Product")
                .price(BigDecimal.ZERO)
                .availability(true)
                .build();

        // Then
        assertThat(product.price()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should be equal when all fields are equal - Lombok @Value")
    void shouldBeEqualWhenAllFieldsAreEqual() {
        // Given
        Product product1 = Product.builder()
                .id("1")
                .name("Product")
                .price(new BigDecimal("10.00"))
                .availability(true)
                .build();

        Product product2 = Product.builder()
                .id("1")
                .name("Product")
                .price(new BigDecimal("10.00"))
                .availability(true)
                .build();

        // Then
        assertThat(product1).isEqualTo(product2)
                .hasSameHashCodeAs(product2);
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void shouldNotBeEqualWhenFieldsDiffer() {
        // Given
        Product product1 = Product.builder()
                .id("1")
                .name("Product A")
                .price(new BigDecimal("10.00"))
                .availability(true)
                .build();

        Product product2 = Product.builder()
                .id("2")
                .name("Product B")
                .price(new BigDecimal("20.00"))
                .availability(false)
                .build();

        // Then
        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    @DisplayName("Should have meaningful toString representation - Lombok @Value")
    void shouldHaveMeaningfulToString() {
        // Given
        Product product = Product.builder()
                .id("123")
                .name("Test Product")
                .price(new BigDecimal("49.99"))
                .availability(true)
                .build();

        // When
        String toString = product.toString();

        // Then
        assertThat(toString)
                .contains("123")
                .contains("Test Product")
                .contains("49.99")
                .contains("true");
    }
}

