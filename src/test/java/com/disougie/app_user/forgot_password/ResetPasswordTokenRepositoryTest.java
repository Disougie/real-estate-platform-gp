package com.disougie.app_user.forgot_password;

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
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;

@DataJpaTest
public class ResetPasswordTokenRepositoryTest {

    @Autowired
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ResetPasswordToken testToken;

    @BeforeEach
    void setUp() {
        AppUser testUser = AppUser.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .phone("123456789")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
                
        entityManager.persistAndFlush(testUser);

        testToken = new ResetPasswordToken(
                null, 
                testUser, 
                "reset-token-123", 
                LocalDateTime.now(), 
                LocalDateTime.now().plusMinutes(15), 
                false
        );
        entityManager.persistAndFlush(testToken);
    }

    @AfterEach
    void tearDown() {
        resetPasswordTokenRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find ResetPasswordToken by exact token string")
    void findByToken_ShouldReturnToken() {
        // When
        Optional<ResetPasswordToken> result = resetPasswordTokenRepository.findByToken("reset-token-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("reset-token-123", result.get().getToken());
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent token")
    void findByToken_ShouldReturnEmptyForInvalidToken() {
        // When
        Optional<ResetPasswordToken> result = resetPasswordTokenRepository.findByToken("invalid-token");

        // Then
        assertTrue(result.isEmpty());
    }
}
