package com.disougie.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {
	
	private final RedisTemplate<String, Object> redisTemplate;
	
	public boolean isAllowed(String clientId) {
		String key = "user:" + clientId;
		Integer id = (Integer) redisTemplate.opsForValue().get(key);
		if(id == null) {
			redisTemplate.opsForValue().set(key, 1, 60, TimeUnit.SECONDS);
			return true;
		}
		if(id < 15) {
			redisTemplate.opsForValue().increment(key);
			return true;
		}
		return false;
	}

}
