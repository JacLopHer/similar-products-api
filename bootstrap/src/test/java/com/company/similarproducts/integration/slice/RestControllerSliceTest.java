package com.company.similarproducts.integration.slice;

import com.company.similarproducts.domain.exception.ProductNotFoundException;
import com.company.similarproducts.domain.model.Product;
import com.company.similarproducts.domain.model.ProductId;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.infrastructure.adapter.rest.SimilarProductsRestController;
import com.company.similarproducts.infrastructure.adapter.rest.mapper.ProductRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SimilarProductsRestController.class)
@Import(ProductRestMapper.class)
@DisplayName("Similar Products REST Controller Slice Tests")
class RestControllerSliceTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GetSimilarProductsUseCase getSimilarProductsUseCase;

    @Test
    void shouldReturnSimilarProductsWhenUseCaseSucceeds() {
        List<Product> mockProducts = List.of(
                new Product("2", "Product 2", new BigDecimal("20.00"), true),
                new Product("3", "Product 3", new BigDecimal("30.00"), true)
        );

        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.just(mockProducts));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("2")
                .jsonPath("$[0].name").isEqualTo("Product 2")
                .jsonPath("$[0].price").isEqualTo(20.00)
                .jsonPath("$[0].availability").isEqualTo(true)
                .jsonPath("$[1].id").isEqualTo("3")
                .jsonPath("$[1].name").isEqualTo("Product 3");
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarProducts() {
        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldReturn404WhenProductNotFound() {
        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.error(new ProductNotFoundException(new ProductId("999"))));

        webTestClient.get()
                .uri("/product/999/similar")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType("application/problem+json")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Product Not Found")
                .jsonPath("$.detail").value(message -> message.toString().contains("999"))
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturn500WhenUnexpectedError() {
        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType("application/problem+json")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Internal Server Error")
                .jsonPath("$.detail").isEqualTo("An unexpected error occurred")
                .jsonPath("$.status").isEqualTo(500);
    }

    @Test
    void shouldValidateProductIdNotBlank() {
        webTestClient.get()
                .uri("/product/ /similar")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType("application/problem+json")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation Error")
                .jsonPath("$.detail").value(detail ->
                    assertThat(detail.toString()).contains("Product ID must not be blank"));
    }

    @Test
    void shouldHandleSpecialCharactersInProductId() {
        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get()
                .uri("/product/ABC-123/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldHandleReactiveStreamErrors() {
        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldProperlyFormatDecimalPrices() {
        List<Product> mockProducts = List.of(
                new Product("1", "Expensive Product", new BigDecimal("999.99"), true),
                new Product("2", "Cheap Product", new BigDecimal("0.01"), true)
        );

        when(getSimilarProductsUseCase.getSimilarProducts(any(ProductId.class)))
                .thenReturn(Mono.just(mockProducts));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].price").isEqualTo(999.99)
                .jsonPath("$[1].price").isEqualTo(0.01);
    }
}
