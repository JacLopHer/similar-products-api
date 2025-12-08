package com.company.similarproducts.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class SimilarProductsWireMockFreshIT {

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:2.35.0")
            .withExposedPorts(8080)
            // Copy test resources 'wiremock' into the container so WireMock reliably loads mappings and __files
            .withCopyFileToContainer(MountableFile.forClasspathResource("wiremock"), "/home/wiremock")
            .waitingFor(Wait.forHttp("/__admin").forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(30));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String base = "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
        registry.add("external.apis.product-service.base-url", () -> base);
    }

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    void smokeTest_wiremockContainer_upAndAppResponds() {
        List<Map> body = webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Map.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
    }
}
