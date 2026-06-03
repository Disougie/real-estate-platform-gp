package com.disougie.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestConfig {
	
	@Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
	
	@Bean ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
}
