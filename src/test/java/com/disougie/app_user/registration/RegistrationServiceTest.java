package com.disougie.app_user.registration;

import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.VERIFY_ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.AppUserRole;
import com.disougie.app_user.confirmation_token.ConfirmationToken;
import com.disougie.app_user.confirmation_token.ConfirmationTokenService;
import com.disougie.email.EmailRequest;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest validRequest;
    private AppUser savedUser;
    private ConfirmationToken confirmationToken;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest(
                "John Doe", "john@example.com", "password123", "password123", "123456789"
        );

        savedUser = AppUser.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(AppUserRole.USER)
                .enabled(false)
                .build();

        confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("sample-token-123");
    }

    @Test
    @DisplayName("Should register a new user successfully and send verification email")
    void registerUser_ShouldSaveUserAndSendEmail() {
        // Given
        when(appUserRepository.findByEmailIncludingDeleted("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(confirmationTokenService.generateConfirmationToken(savedUser)).thenReturn(confirmationToken);

        // When
        RegistrationResponse response = registrationService.registerUser(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        
        verify(appUserRepository).save(any(AppUser.class));
        verify(rabbitTemplate).convertAndSend(
                eq(EMAIL_EXCHANGE),
                eq(VERIFY_ROUTING_KEY),
                any(EmailRequest.class)
        );
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if email is already taken")
    void registerUser_ShouldThrowExceptionIfEmailExists() {
        // Given
        when(appUserRepository.findByEmailIncludingDeleted("john@example.com"))
                .thenReturn(Optional.of(savedUser));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> registrationService.registerUser(validRequest));
        
        assertTrue(exception.getMessage().contains("Email already taken"));
        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(EmailRequest.class));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if passwords do not match")
    void registerUser_ShouldThrowExceptionIfPasswordsMismatch() {
        // Given
        RegistrationRequest mismatchRequest = new RegistrationRequest(
                "John Doe", "john@example.com", "password123", "differentPassword", "123456789"
        );
        when(appUserRepository.findByEmailIncludingDeleted("john@example.com")).thenReturn(Optional.empty());

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> registrationService.registerUser(mismatchRequest));
        
        assertTrue(exception.getMessage().contains("password does not match"));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

}
