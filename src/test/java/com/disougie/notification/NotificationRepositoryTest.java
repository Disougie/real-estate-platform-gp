package com.disougie.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

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
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser recipient;

    @BeforeEach
    void setUp() {
        recipient = AppUser.builder()
                .name("Test User")
                .phone("0123456789")
                .email("recipient@test.com")
                .password("password")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
        entityManager.persistAndFlush(recipient);

        Notification notif1 = new Notification();
        notif1.setRecipient(recipient);
        notif1.setMessage("First message");
        notif1.setCreated_at(LocalDateTime.now().minusDays(2));
        entityManager.persistAndFlush(notif1);

        Notification notif2 = new Notification();
        notif2.setRecipient(recipient);
        notif2.setMessage("Second message");
        notif2.setCreated_at(LocalDateTime.now().minusDays(1)); // more recent
        entityManager.persistAndFlush(notif2);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find notifications by recipient ordered by date descending")
    void findByRecipient_ShouldReturnPagedNotificationsOrdered() {
        // When
        Page<Notification> results = notificationRepository.findByRecipient(recipient, PageRequest.of(0, 10));

        // Then
        assertNotNull(results);
        assertEquals(2, results.getTotalElements());
        // Second message is more recent, should be first
        assertEquals("Second message", results.getContent().get(0).getMessage());
        assertEquals("First message", results.getContent().get(1).getMessage());
    }
}
