package com.company.similarproducts.integration.base;

import org.junit.jupiter.api.BeforeEach;
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

@Testcontainers
@SpringBootTest(
        classes = com.company.similarproducts.SimilarProductsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {

    @Container
    protected static final GenericContainer<?> WIREMOCK_CONTAINER = new GenericContainer<>("wiremock/wiremock:3.3.1")
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("wiremock"),
                    "/home/wiremock"
            )
            .waitingFor(Wait.forHttp("/__admin").forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(30));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://" + WIREMOCK_CONTAINER.getHost() + ":" + WIREMOCK_CONTAINER.getMappedPort(8080);
        registry.add("external-apis.product-service.base-url", () -> baseUrl);
        registry.add("external.apis.product-service.base-url", () -> baseUrl);
        registry.add("external-apis.product-service.timeout", () -> "5000");
    }

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    @BeforeEach
    protected void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    protected String getWireMockBaseUrl() {
        return "http://" + WIREMOCK_CONTAINER.getHost() + ":" + WIREMOCK_CONTAINER.getMappedPort(8080);
    }

    protected String getApplicationBaseUrl() {
        return "http://localhost:" + port;
    }
}
