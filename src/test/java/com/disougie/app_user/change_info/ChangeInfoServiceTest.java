package com.disougie.app_user.change_info;

import static com.disougie.email.EmailMQConstant.CHANGE_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.confirmation_token.ConfirmationToken;
import com.disougie.app_user.confirmation_token.ConfirmationTokenService;
import com.disougie.email.EmailRequest;
import com.disougie.security.JwtService;
import com.disougie.util.TimeUtil;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class ChangeInfoServiceTest {

    @Mock
    private ChangeEmailRepository changeEmailRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ChangeInfoService changeInfoService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser mockUser;
    private ConfirmationToken mockToken;

    @BeforeEach
    void setUp() {
        mockUser = AppUser.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .build();
                
        mockToken = new ConfirmationToken("sample-token", mockUser);
        
        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should successfully change email request for first time")
    void changeEmail_ShouldCreateNewChangeEmailRequest() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(changeEmailRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(confirmationTokenService.generateConfirmationToken(mockUser)).thenReturn(mockToken);

        ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

        // When
        changeInfoService.changeEmail(request);

        // Then
        verify(changeEmailRepository).save(any(ChangeEmail.class));
        verify(rabbitTemplate).convertAndSend(
                eq(EMAIL_EXCHANGE),
                eq(CHANGE_ROUTING_KEY),
                any(EmailRequest.class)
        );
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if admin tries to change email")
    void changeEmail_ShouldThrowExceptionForMainAdmin() {
        // Given
        AppUser adminUser = AppUser.builder().email("admin@system.com").build();
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(adminUser);

        ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> changeInfoService.changeEmail(request));
        assertTrue(exception.getMessage().contains("main admin email can not be change"));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if change email requested within a day")
    void changeEmail_ShouldThrowExceptionIfRequestedTooSoon() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        ChangeEmail existingChange = new ChangeEmail(1L, mockUser, "new@example.com", TimeUtil.now().minusHours(12), mockToken);
        when(changeEmailRepository.findByUser(mockUser)).thenReturn(Optional.of(existingChange));

        ChangeEmailRequest request = new ChangeEmailRequest("another@example.com");

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> changeInfoService.changeEmail(request));
        assertTrue(exception.getMessage().contains("only once per day allowed"));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(EmailRequest.class));
    }

    @Test
    @DisplayName("Should successfully change password if old password matches")
    void changePassword_ShouldUpdatePassword() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("oldPass123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");

        // When
        changeInfoService.changePassword(request);

        // Then
        verify(appUserRepository).save(mockUser);
        assertTrue(mockUser.getPassword().equals("encodedNewPassword"));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if old password does not match during password change")
    void changePassword_ShouldThrowExceptionForIncorrectPassword() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass123");

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> changeInfoService.changePassword(request));
        assertTrue(exception.getMessage().contains("incorrect password"));
    }

    @Test
    @DisplayName("Should successfully change phone if password matches")
    void changePhone_ShouldUpdatePhone() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("validPass123", "encodedPassword")).thenReturn(true);

        ChangePhoneRequest request = new ChangePhoneRequest("987654321", "validPass123");

        // When
        changeInfoService.changePhone(request);

        // Then
        verify(appUserRepository).save(mockUser);
        assertTrue(mockUser.getPhone().equals("987654321"));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if password does not match during phone change")
    void changePhone_ShouldThrowExceptionForIncorrectPassword() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        ChangePhoneRequest request = new ChangePhoneRequest("987654321", "wrongPass");

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> changeInfoService.changePhone(request));
        assertTrue(exception.getMessage().contains("incorrect password"));
    }
}
