package com.company.similarproducts.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "external-apis.product-service.base-url=http://localhost:3001",
    "logging.level.root=WARN"
})
class VersionControllerIT {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void shouldReturnVersionInformation() {
        // Given
        String url = "http://localhost:" + port + "/api/v1/version";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Similar Products API");
        assertThat(response.getBody()).contains("version");
        assertThat(response.getBody()).contains("buildTime");
        assertThat(response.getBody()).contains("gitCommit");
        assertThat(response.getBody()).contains("gitBranch");
    }

    @Test
    void shouldReturnDetailedInfo() {
        // Given
        String url = "http://localhost:" + port + "/api/v1/info";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Similar Products API");
        assertThat(response.getBody()).contains("application");
        assertThat(response.getBody()).contains("description");
    }
}
