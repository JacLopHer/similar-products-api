package com.company.similarproducts.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

    @Test
    void shouldCreateProductIdWithValidValue() {
        String validId = "123";
        ProductId productId = new ProductId(validId);

        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(validId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void shouldThrowExceptionWhenProductIdIsInvalid(String invalidId) {
        assertThatThrownBy(() -> new ProductId(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or empty");
    }

    @Test
    void shouldReturnValueAsStringRepresentation() {
        String id = "12345";
        ProductId productId = new ProductId(id);
        String result = productId.toString();

        assertThat(result).isEqualTo(id);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        ProductId productId1 = new ProductId("123");
        ProductId productId2 = new ProductId("123");

        assertThat(productId1).isEqualTo(productId2)
                .hasSameHashCodeAs(productId2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        ProductId productId1 = new ProductId("123");
        ProductId productId2 = new ProductId("456");

        assertThat(productId1).isNotEqualTo(productId2);
    }

    @Test
    void shouldHandleDifferentIdFormats() {
        assertThatNoException().isThrownBy(() -> {
            new ProductId("1");
            new ProductId("abc123");
            new ProductId("PROD-12345");
            new ProductId("123-456-789");
        });
    }
}


