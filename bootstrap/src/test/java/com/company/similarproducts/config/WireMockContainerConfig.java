package com.company.similarproducts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

/**
 * Test configuration that starts a WireMock container (Testcontainers) and sets
 * the system property EXTERNAL_PRODUCT_SERVICE_BASE_URL so the application under test
 * points to the container.
 */
@TestConfiguration
public class WireMockContainerConfig {

    private static final GenericContainer<?> WIREMOCK_CONTAINER;

    static {
        WIREMOCK_CONTAINER = new GenericContainer<>("wiremock/wiremock:2.35.0")
                .withExposedPorts(8080)
                .withCopyFileToContainer(MountableFile.forClasspathResource("wiremock"), "/home/wiremock")
                .waitingFor(Wait.forHttp("/__admin").forStatusCode(200))
                .withStartupTimeout(Duration.ofSeconds(120));

        // Start container synchronously so property is available before Spring context initializes
        WIREMOCK_CONTAINER.start();

        String baseUrl = "http://" + WIREMOCK_CONTAINER.getHost() + ":" + WIREMOCK_CONTAINER.getMappedPort(8080);
        System.setProperty("EXTERNAL_PRODUCT_SERVICE_BASE_URL", baseUrl);

        // Register shutdown hook to stop the container when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                WIREMOCK_CONTAINER.stop();
            } catch (Exception ignored) {
            }
        }));
    }

    @Bean(destroyMethod = "stop")
    public GenericContainer<?> wireMockContainer() {
        return WIREMOCK_CONTAINER;
    }
}

