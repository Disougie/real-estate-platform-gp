package com.disougie.app_user.confirmation_token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;

@DataJpaTest
public class ConfirmationTokenRepositoryTest {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser testUser;
    private ConfirmationToken testToken;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .phone("123456789")
                .role(AppUserRole.USER)
                .enabled(false)
                .build();
                
        entityManager.persistAndFlush(testUser);

        testToken = new ConfirmationToken("sample-token-123", testUser);
        entityManager.persistAndFlush(testToken);
    }

    @AfterEach
    void tearDown() {
        confirmationTokenRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find ConfirmationToken by exact token string")
    void findByToken_ShouldReturnConfirmationToken() {
        // When
        Optional<ConfirmationToken> result = confirmationTokenRepository.findByToken("sample-token-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("sample-token-123", result.get().getToken());
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent token")
    void findByToken_ShouldReturnEmptyForInvalidToken() {
        // When
        Optional<ConfirmationToken> result = confirmationTokenRepository.findByToken("invalid-token");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find ConfirmationToken by associated AppUser")
    void findByAppUser_ShouldReturnConfirmationToken() {
        // When
        Optional<ConfirmationToken> result = confirmationTokenRepository.findByAppUser(testUser);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }
}
