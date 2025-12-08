package com.company.similarproducts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Objects;

/**
 * Test configuration that starts a lightweight Reactor Netty HTTP server on a random port
 * and exposes mock endpoints used by integration tests. Sets system property
 * EXTERNAL_PRODUCT_SERVICE_BASE_URL so the application under test targets this mock.
 */
@TestConfiguration
public class WireMockConfig {

    private static final DisposableServer SERVER;

    static {
        SERVER = HttpServer.create()
                .host("localhost")
                .port(0)
                .handle(WireMockConfig::handleRequest)
                .bindNow();

        String baseUrl = "http://localhost:" + SERVER.port();
        System.setProperty("EXTERNAL_PRODUCT_SERVICE_BASE_URL", baseUrl);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                SERVER.disposeNow();
            } catch (Exception ignored) {
            }
        }));
    }

    private static reactor.core.publisher.Mono<Void> handleRequest(HttpServerRequest request, HttpServerResponse response) {
        String uri = request.uri();

        try {
            // similarids for product 1 (happy path)
            if (Objects.equals(uri, "/product/1/similarids")) {
                String body = "[\"2\", \"3\", \"4\"]";
                response.header("Content-Type", "application/json");
                response.status(200);
                return response.sendString(Mono.just(body)).then();
            }

            // similarids empty for product 2
            if (Objects.equals(uri, "/product/2/similarids")) {
                String body = "[]";
                response.header("Content-Type", "application/json");
                response.status(200);
                return response.sendString(Mono.just(body)).then();
            }

            // similarids returns 500 for product 3 to exercise error handling
            if (Objects.equals(uri, "/product/3/similarids")) {
                String body = "{\"error\":\"internal\"}";
                response.header("Content-Type", "application/json");
                response.status(500);
                return response.sendString(Mono.just(body)).then();
            }

            // similarids partial: product 4 returns an id that doesn't exist (999)
            if (Objects.equals(uri, "/product/4/similarids")) {
                String body = "[\"2\", \"999\"]";
                response.header("Content-Type", "application/json");
                response.status(200);
                return response.sendString(Mono.just(body)).then();
            }

            // product details for 1..4
            if (uri.equals("/product/1") || uri.equals("/product/2") || uri.equals("/product/3") || uri.equals("/product/4") || uri.equals("/product/999")) {
                String id = uri.substring(uri.lastIndexOf('/') + 1);
                if ("999".equals(id)) {
                    // simulate not found for 999
                    response.status(404);
                    return response.send();
                }

                String body = "{\"id\":\"" + id + "\",\"name\":\"Product " + id + "\",\"price\":" + (Integer.parseInt(id) * 10) + ",\"availability\":true}";
                if ("3".equals(id)) { // make product 3 unavailable to exercise variations
                    body = "{\"id\":\"3\",\"name\":\"Product 3\",\"price\":30.0,\"availability\":false}";
                }
                response.header("Content-Type", "application/json");
                response.status(200);
                return response.sendString(Mono.just(body)).then();
            }

            response.status(404);
            return response.send();
        } catch (Exception e) {
            response.status(500);
            return response.send();
        }
    }

    @Bean(destroyMethod = "disposeNow")
    public DisposableServer testServer() {
        return SERVER;
    }
}
