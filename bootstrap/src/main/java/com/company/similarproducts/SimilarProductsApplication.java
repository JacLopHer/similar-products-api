package com.company.similarproducts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application - Entry point.
 * Scans all infrastructure adapters and configuration.
 */
@SpringBootApplication(scanBasePackages = "com.company.similarproducts")
public class SimilarProductsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimilarProductsApplication.class, args);
    }
}
