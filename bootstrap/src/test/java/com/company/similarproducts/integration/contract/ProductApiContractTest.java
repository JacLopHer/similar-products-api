package com.company.similarproducts.integration.contract;

import com.company.similarproducts.integration.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product API Contract Tests")
class ProductApiContractTest extends BaseIntegrationTest {

    private List<Map<String, Object>> getSimilarProducts(String productId) {
        return webTestClient.get()
                .uri("/product/{productId}/similar", productId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .returnResult()
                .getResponseBody();
    }

    @Test
    void shouldHandleSuccessfulSimilarProductsContract() {
        List<Map<String, Object>> response = getSimilarProducts("1");

        assertThat(response).isNotNull().hasSize(3);

        response.forEach(product -> {
            assertThat(product).containsKeys("id", "name", "price", "availability");
            assertThat(product.get("id")).isInstanceOf(String.class);
            assertThat(product.get("name")).isInstanceOf(String.class);
            assertThat(product.get("price")).isInstanceOf(Number.class);
            assertThat(product.get("availability")).isInstanceOf(Boolean.class);
        });

        List<String> productIds = response.stream()
                .map(m -> m.get("id").toString())
                .toList();
        assertThat(productIds).containsExactlyInAnyOrder("2", "3", "4");
    }

    @Test
    void shouldHandleProductNotFoundContract() {
        webTestClient.get()
                .uri("/product/999/similar")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").exists()
                .jsonPath("$.detail").exists()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldHandleEmptySimilarProductsContract() {
        List<Map<String, Object>> response = getSimilarProducts("2");

        assertThat(response).isNotNull().isEmpty();
    }

    @Test
    void shouldHandlePartialAvailableProductsContract() {
        List<Map<String, Object>> response = getSimilarProducts("4");

        assertThat(response).isNotNull().hasSize(1);

        Map<String, Object> product = response.get(0);
        assertThat(product.get("id")).hasToString("2");
        assertThat(product).containsKeys("id", "name", "price", "availability");
    }

    @Test
    void shouldHandleSimilarIdsServiceErrorContract() {
        List<Map<String, Object>> response = getSimilarProducts("3");

        assertThat(response).isNotNull().isEmpty();
    }

    @Test
    void shouldHandleInvalidProductIdContract() {
        webTestClient.get()
                .uri("/product/ /similar")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").exists()
                .jsonPath("$.detail").exists();
    }
}
