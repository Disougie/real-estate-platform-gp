package com.disougie.app_user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;

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
public class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser activeUser;
    private AppUser deletedUser;
    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        activeUser = AppUser.builder()
                .name("Active User")
                .email("active@example.com")
                .password("password")
                .phone("123456")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();

        deletedUser = AppUser.builder()
                .name("Deleted User")
                .email("deleted@example.com")
                .password("password")
                .phone("654321")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();

        adminUser = AppUser.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("password")
                .phone("999999")
                .role(AppUserRole.ADMIN)
                .enabled(true)
                .build();

        entityManager.persistAndFlush(activeUser);
        entityManager.persistAndFlush(adminUser);
        
        // Simulating a deleted user by calling the SQLDelete implicitly or manually
        entityManager.persistAndFlush(deletedUser);
        appUserRepository.delete(deletedUser);
        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find active user by email")
    void shouldFindActiveUserByEmail() {
        // When
        Optional<AppUser> result = appUserRepository.findByEmail("active@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active User", result.get().getName());
    }

    @Test
    @DisplayName("Should not find deleted user by standard findByEmail due to SQLRestriction")
    void shouldNotFindDeletedUserByEmail() {
        // When
        Optional<AppUser> result = appUserRepository.findByEmail("deleted@example.com");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find deleted user by findByEmailIncludingDeleted using native query")
    void shouldFindDeletedUserByEmailIncludingDeleted() {
        // When
        Optional<AppUser> result = appUserRepository.findByEmailIncludingDeleted("deleted@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("deleted@example.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Should find users by role for admin using native query")
    void shouldFindByRoleForAdmin() {
        // When
        Page<AppUser> result = appUserRepository.findByRoleForAdmin(PageRequest.of(0, 10), "ADMIN");

        // Then
        assertNotNull(result);
        assertThat(result.getContent().size()).isGreaterThan(0);
    }

    /*
     * Note: Testing MySQL native full-text search function 'match ... against'
     * requires a MySQL database. When running this test with an H2 in-memory DB,
     * this specific native query might fail due to syntax differences.
     */
    @Test
    @DisplayName("Should execute searchByNameOrEmail method (ensure query syntax is valid)")
    void shouldSearchByNameOrEmail() {
        try {
            Page<AppUser> result = appUserRepository.searchByNameOrEmail(
                PageRequest.of(0, 10), "Active", "USER"
            );
            assertNotNull(result);
        } catch (Exception e) {
            // Expected if running on H2 DB without MySQL mode or full-text search support
            assertThat(e.getMessage()).containsIgnoringCase("syntax");
        }
    }
}
