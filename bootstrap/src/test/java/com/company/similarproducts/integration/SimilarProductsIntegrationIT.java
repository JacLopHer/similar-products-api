package com.company.similarproducts.integration;

import com.company.similarproducts.config.WireMockContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Import(WireMockContainerConfig.class)
class SimilarProductsIntegrationIT {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void whenProductExists_thenReturnSimilarProducts() {
        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(Map.class)
                .consumeWith(response -> {
                    List<Map> body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body).hasSize(3);

                    List<String> ids = body.stream()
                            .map(m -> ((Map<String, Object>) m).get("id").toString())
                            .toList();

                    assertThat(ids).containsExactlyInAnyOrder("2", "3", "4");
                });
    }
}
