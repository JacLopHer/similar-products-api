package com.company.similarproducts.config;

import com.company.similarproducts.application.service.GetSimilarProductsService;
import com.company.similarproducts.domain.port.GetSimilarProductsUseCase;
import com.company.similarproducts.domain.port.LoadProductPort;
import com.company.similarproducts.domain.port.LoadSimilarProductIdsPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dependency Injection Configuration.
 * Wires domain, application and infrastructure layers.
 * This is where the Hexagonal Architecture comes together.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Creates the Use Case implementation.
     * Application service depends on domain ports (interfaces).
     * Infrastructure adapters implement those ports.
     */
    @Bean
    public GetSimilarProductsUseCase getSimilarProductsUseCase(
            LoadProductPort loadProductPort,
            LoadSimilarProductIdsPort loadSimilarProductIdsPort) {
        
        return new GetSimilarProductsService(loadProductPort, loadSimilarProductIdsPort);
    }
}
