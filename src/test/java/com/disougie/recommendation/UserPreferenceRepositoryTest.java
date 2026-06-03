package com.disougie.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;

import com.disougie.config.TestConfig;
import com.disougie.redis.RateLimitFilter;

@DataMongoTest(
	excludeAutoConfiguration = {
    		SecurityAutoConfiguration.class, 
    	    UserDetailsServiceAutoConfiguration.class
    },
	excludeFilters = {
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class),
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class) // استبعاد الفلتر
	}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
public class UserPreferenceRepositoryTest {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    private UserPreference testPreference;

    @BeforeEach
    void setUp() {
        testPreference = UserPreference.builder()
                .userId(1L)
                .preferencedCity(Map.of("Cairo", 5))
                .preferencedArea(Map.of("Maadi", 3))
                .preferencedType(Map.of("RENT", 2))
                .preferencedPrice(5000.0)
                .preferencedSize(120)
                .preferencedCont(10)
                .build();
                
        userPreferenceRepository.save(testPreference);
    }

    @AfterEach
    void tearDown() {
        userPreferenceRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find UserPreference by userId")
    void findByUserId_ShouldReturnPreference() {
        // When
        Optional<UserPreference> result = userPreferenceRepository.findByUserId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
        assertEquals(5000.0, result.get().getPreferencedPrice());
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent userId")
    void findByUserId_ShouldReturnEmptyForInvalidId() {
        // When
        Optional<UserPreference> result = userPreferenceRepository.findByUserId(99L);

        // Then
        assertTrue(result.isEmpty());
    }
}
