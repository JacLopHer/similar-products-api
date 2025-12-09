package com.company.similarproducts.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high performance.
 * Caches product data and similar product IDs to reduce external API calls.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache names used in the application
     */
    public static final String PRODUCTS_CACHE = "products";
    public static final String SIMILAR_IDS_CACHE = "similarIds";

    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine cache manager");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PRODUCTS_CACHE, SIMILAR_IDS_CACHE);

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());

        log.info("Cache configured: maxSize=10000, TTL=5min");
        return cacheManager;
    }
}

