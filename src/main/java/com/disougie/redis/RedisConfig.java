package com.disougie.redis;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
	
	@Bean
	RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory ConnectionFactory
	){
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(ConnectionFactory);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(RedisSerializer.json());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
	
	@Bean
	RedisCacheManager redisCacheManager(
			RedisConnectionFactory ConnectionFactory
	) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration
				.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(10))
				.serializeKeysWith(
						RedisSerializationContext
							.SerializationPair
							.fromSerializer(RedisSerializer.string())
				)
				.serializeValuesWith(
						RedisSerializationContext
							.SerializationPair
							.fromSerializer(RedisSerializer.json())
				);
		
		return RedisCacheManager
				.builder(ConnectionFactory)
				.cacheDefaults(configuration)
				.build();
				
	}

}
