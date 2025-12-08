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
            if (Objects.equals(uri, "/product/1/similarids")) {
                String body = "[\"2\", \"3\", \"4\"]";
                response.header("Content-Type", "application/json");
                response.status(200);
                return response.sendString(Mono.just(body)).then();
            }

            if (uri.equals("/product/1") || uri.equals("/product/2") || uri.equals("/product/3") || uri.equals("/product/4")) {
                String id = uri.substring(uri.lastIndexOf('/') + 1);
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
