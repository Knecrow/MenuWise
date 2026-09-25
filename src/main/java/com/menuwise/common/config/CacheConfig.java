package com.menuwise.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Cache and HTTP client configuration for weather signals and prep recommendations.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("weatherForecast", "dailyPrepRecommendations");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
