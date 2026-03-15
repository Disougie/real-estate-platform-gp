package com.disougie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RealEstatePlatformGpApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealEstatePlatformGpApplication.class, args);
	}

}
