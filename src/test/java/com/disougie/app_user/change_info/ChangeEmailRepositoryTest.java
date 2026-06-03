package com.disougie.app_user.change_info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;
import com.disougie.app_user.confirmation_token.ConfirmationToken;
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
public class ChangeEmailRepositoryTest {

    @Autowired
    private ChangeEmailRepository changeEmailRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser testUser;
    private ConfirmationToken testToken;
    private ChangeEmail testChangeEmail;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .name("Test User")
                .phone("0123456789")
                .email("old@example.com")
                .password("password123")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
                
        entityManager.persistAndFlush(testUser);

        testToken = new ConfirmationToken("sample-token-123", testUser);
        entityManager.persistAndFlush(testToken);
        
        testChangeEmail = new ChangeEmail(
                null, 
                testUser, 
                "new@example.com", 
                LocalDateTime.now(), 
                testToken
        );
        entityManager.persistAndFlush(testChangeEmail);
    }

    @AfterEach
    void tearDown() {
        changeEmailRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find ChangeEmail request by AppUser")
    void findByUser_ShouldReturnChangeEmail() {
        // When
        Optional<ChangeEmail> result = changeEmailRepository.findByUser(testUser);

        // Then
        assertTrue(result.isPresent());
        assertEquals("new@example.com", result.get().getNewEmail());
    }

    @Test
    @DisplayName("Should find ChangeEmail request by ConfirmationToken")
    void findByConfirmationToken_ShouldReturnChangeEmail() {
        // When
        Optional<ChangeEmail> result = changeEmailRepository.findByConfirmationToken(testToken);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testToken.getId(), result.get().getConfirmationToken().getId());
    }
}
