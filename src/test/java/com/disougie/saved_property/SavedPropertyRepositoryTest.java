package com.disougie.saved_property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;
import com.disougie.config.TestConfig;
import com.disougie.redis.RateLimitFilter;

@DataJpaTest(
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
public class SavedPropertyRepositoryTest {

    @Autowired
    private SavedPropertyRepository savedPropertyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser user1;
    private AppUser user2;
    private SavedProperty savedProperty1;

    @BeforeEach
    void setUp() {
        user1 = AppUser.builder()
                .name("User One")
                .email("user1@test.com")
                .password("password")
                .phone("11111111")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
        
        user2 = AppUser.builder()
                .name("User Two")
                .email("user2@test.com")
                .password("password")
                .phone("22222222")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
                
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        SavedPropertyId id1 = new SavedPropertyId(user1.getId(), "prop-1");
        savedProperty1 = new SavedProperty(id1, user1);
        
        entityManager.persistAndFlush(savedProperty1);
    }

    @AfterEach
    void tearDown() {
        savedPropertyRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find saved properties by user")
    void shouldFindByUser() {
        // When
        List<SavedProperty> result = savedPropertyRepository.findByUser(user1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prop-1", result.get(0).getId().getPropertyId());
        assertEquals(user1.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("Should return empty list for user with no saved properties")
    void shouldReturnEmptyListForUserWithNoSavedProperties() {
        // When
        List<SavedProperty> result = savedPropertyRepository.findByUser(user2);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
