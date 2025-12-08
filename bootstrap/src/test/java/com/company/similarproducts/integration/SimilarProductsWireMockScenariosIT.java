package com.company.similarproducts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class SimilarProductsWireMockScenariosIT {

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:2.35.0")
            .withExposedPorts(8080)
            .withCopyFileToContainer(MountableFile.forClasspathResource("wiremock"), "/home/wiremock")
            .waitingFor(Wait.forHttp("/__admin").forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(120));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String base = "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
        // Register the exact key the WebClient expects
        registry.add("external-apis.product-service.base-url", () -> base);
    }

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void productNotFound_returns404() {
        webTestClient.get()
                .uri("/product/999/similar")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void productWithNoSimilar_returnsEmptyList() {
        List<Map> body = webTestClient.get().uri("/product/2/similar").exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull().isEmpty();
    }

    @Test
    void similarIdsError_returnsEmptyList() {
        List<Map> body = webTestClient.get().uri("/product/3/similar").exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull().isEmpty();
    }

    @Test
    void partialAvailable_returnsOnlyFound() {
        List<Map> body = webTestClient.get().uri("/product/4/similar").exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull().hasSize(1);
        assertThat(body.get(0).get("id").toString()).hasToString("2");
    }
}
