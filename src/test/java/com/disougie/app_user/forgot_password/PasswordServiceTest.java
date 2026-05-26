package com.disougie.app_user.forgot_password;

import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.RESET_ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.email.EmailRequest;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PasswordService passwordService;

    private AppUser mockUser;
    private ResetPasswordToken mockToken;
    private final String website = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordService, "website", website);
        
        mockUser = AppUser.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("oldPassword")
                .build();
                
        mockToken = new ResetPasswordToken(
                1L, 
                mockUser, 
                "reset-token-123", 
                LocalDateTime.now(), 
                LocalDateTime.now().plusMinutes(15), 
                false
        );
    }

    @Test
    @DisplayName("Should generate token and send email when forgot password requested")
    void forgotPassword_ShouldSaveTokenAndSendEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
        when(appUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));

        // When
        passwordService.forgotPassword(request);

        // Then
        verify(resetPasswordTokenRepository).save(any(ResetPasswordToken.class));
        verify(rabbitTemplate).convertAndSend(
                eq(EMAIL_EXCHANGE),
                eq(RESET_ROUTING_KEY),
                any(EmailRequest.class)
        );
    }

    @Test
    @DisplayName("Should silently return if user is not found during forgot password")
    void forgotPassword_ShouldReturnIfUserNotFound() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
        when(appUserRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When
        passwordService.forgotPassword(request);

        // Then
        verify(resetPasswordTokenRepository, never()).save(any(ResetPasswordToken.class));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(EmailRequest.class));
    }

    @Test
    @DisplayName("Should reset password successfully and mark token as used")
    void resetPassword_ShouldUpdatePasswordAndMarkTokenUsed() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-123", "newPassword123", "newPassword123");
        when(resetPasswordTokenRepository.findByToken("reset-token-123")).thenReturn(Optional.of(mockToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // When
        String result = passwordService.resetPassword(request);

        // Then
        assertEquals(website + "/login", result);
        assertEquals("encodedNewPassword", mockUser.getPassword());
        assertTrue(mockToken.isUsed());
        verify(appUserRepository).save(mockUser);
        verify(resetPasswordTokenRepository).save(mockToken);
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if token is already used")
    void resetPassword_ShouldThrowExceptionIfTokenIsUsed() {
        // Given
        mockToken.setUsed(true);
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-123", "pass", "pass");
        when(resetPasswordTokenRepository.findByToken("reset-token-123")).thenReturn(Optional.of(mockToken));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> passwordService.resetPassword(request));
        assertTrue(exception.getMessage().contains("already been used"));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if token is expired")
    void resetPassword_ShouldThrowExceptionIfTokenIsExpired() {
        // Given
        mockToken.setExpireAt(LocalDateTime.now().minusMinutes(5));
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-123", "pass", "pass");
        when(resetPasswordTokenRepository.findByToken("reset-token-123")).thenReturn(Optional.of(mockToken));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> passwordService.resetPassword(request));
        assertTrue(exception.getMessage().contains("token expired"));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if passwords do not match")
    void resetPassword_ShouldThrowExceptionIfPasswordsMismatch() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-123", "pass123", "pass456");
        when(resetPasswordTokenRepository.findByToken("reset-token-123")).thenReturn(Optional.of(mockToken));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> passwordService.resetPassword(request));
        assertTrue(exception.getMessage().contains("password is not equals its confirmation"));
    }
}
