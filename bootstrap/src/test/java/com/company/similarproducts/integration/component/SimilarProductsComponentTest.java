package com.company.similarproducts.integration.component;

import com.company.similarproducts.integration.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Similar Products Component Tests")
@TestPropertySource(properties = {
    "external-apis.product-service.timeout=10000",
    "spring.main.allow-bean-definition-overriding=true"
})
class SimilarProductsComponentTest extends BaseIntegrationTest {

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
    void shouldGetSimilarProductsCompleteFlow() {
        List<Map<String, Object>> similarProducts = getSimilarProducts("1");

        assertThat(similarProducts).isNotNull().hasSize(3);

        similarProducts.forEach(product -> {
            assertThat(product.get("id")).isNotNull();
            assertThat(product.get("name")).isNotNull();
            assertThat(product.get("price")).isNotNull();
            assertThat(product.get("availability")).isInstanceOf(Boolean.class);
        });

        List<String> productIds = similarProducts.stream()
                .map(p -> p.get("id").toString())
                .sorted()
                .toList();
        assertThat(productIds).containsExactly("2", "3", "4");
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarProducts() {
        List<Map<String, Object>> similarProducts = getSimilarProducts("2");
        assertThat(similarProducts).isNotNull().isEmpty();
    }

    @Test
    void shouldReturn404ForNonExistentProduct() {
        webTestClient.get()
                .uri("/product/999/similar")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Product Not Found")
                .jsonPath("$.detail").value(message ->
                    assertThat(message.toString()).contains("999")
                )
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldHandleExternalServiceErrorsGracefully() {
        List<Map<String, Object>> similarProducts = getSimilarProducts("3");
        assertThat(similarProducts).isNotNull().isEmpty();
    }

    @Test
    void shouldHandlePartiallyAvailableProducts() {
        List<Map<String, Object>> similarProducts = getSimilarProducts("4");

        assertThat(similarProducts).isNotNull().hasSize(1);

        Map<String, Object> availableProduct = similarProducts.get(0);
        assertThat(availableProduct.get("id")).hasToString("2");
        assertThat(availableProduct).containsEntry("availability", true);
    }

    @Test
    void shouldRejectBlankProductId() {
        webTestClient.get()
                .uri("/product/{productId}/similar", "")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldRespondWithinAcceptableTime() {
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> similarProducts = webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .returnResult()
                .getResponseBody();

        long responseTime = System.currentTimeMillis() - startTime;

        assertThat(similarProducts).isNotEmpty().isNotNull();
        assertThat(responseTime).isLessThan(5000);
    }
}
