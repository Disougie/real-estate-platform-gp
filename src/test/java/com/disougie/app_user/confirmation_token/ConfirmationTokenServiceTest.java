package com.disougie.app_user.confirmation_token;

import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.VERIFY_ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.change_info.ChangeEmail;
import com.disougie.app_user.change_info.ChangeEmailRepository;
import com.disougie.email.EmailRequest;
import com.disougie.util.TimeUtil;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class ConfirmationTokenServiceTest {

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ChangeEmailRepository changeEmailRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ConfirmationTokenService confirmationTokenService;

    private AppUser mockUser;
    private ConfirmationToken mockToken;
    private final String website = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(confirmationTokenService, "website", website);
        
        mockUser = AppUser.builder()
                .id(1L)
                .name("Test User")
                .phone("0123456789")
                .email("user@test.com")
                .enabled(false)
                .build();
                
        mockToken = new ConfirmationToken("sample-token", mockUser);
    }

    @Test
    @DisplayName("Should generate and save confirmation token")
    void generateConfirmationToken_ShouldReturnToken() {
        // Given
        when(confirmationTokenRepository.save(any(ConfirmationToken.class))).thenReturn(mockToken);

        // When
        ConfirmationToken result = confirmationTokenService.generateConfirmationToken(mockUser);

        // Then
        assertNotNull(result);
        assertEquals("sample-token", result.getToken());
        verify(confirmationTokenRepository).save(any(ConfirmationToken.class));
    }

    @Test
    @DisplayName("Should verify token successfully and enable user")
    void verifyToken_ShouldReturnWebsiteLoginUrl() {
        // Given
        when(confirmationTokenRepository.findByToken("sample-token")).thenReturn(Optional.of(mockToken));

        // When
        String url = confirmationTokenService.verifyToken("sample-token");

        // Then
        assertEquals(website + "/login", url);
        assertTrue(mockUser.isEnabled());
        assertNotNull(mockToken.getConfirmedAt());
        verify(confirmationTokenRepository).save(mockToken);
        verify(appUserRepository).save(mockUser);
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if token is expired when verifying")
    void verifyToken_ShouldThrowExceptionForExpiredToken() {
        // Given
        mockToken.setExpireAt(TimeUtil.now().minusMinutes(5));
        when(confirmationTokenRepository.findByToken("sample-token")).thenReturn(Optional.of(mockToken));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> confirmationTokenService.verifyToken("sample-token"));
        assertTrue(exception.getMessage().contains("token has been expired"));
    }

    @Test
    @DisplayName("Should resend token and dispatch email")
    void resendToken_ShouldUpdateTokenAndSendEmail() {
        // Given
        ResendTokenRequest request = new ResendTokenRequest("user@test.com");
        mockToken.setExpireAt(TimeUtil.now().minusMinutes(20)); // ensure it's expired so it can be resent
        when(appUserRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(confirmationTokenRepository.findByAppUser(mockUser)).thenReturn(Optional.of(mockToken));

        // When
        confirmationTokenService.resendToken(request);

        // Then
        verify(confirmationTokenRepository).save(mockToken);
        verify(rabbitTemplate).convertAndSend(
                eq(EMAIL_EXCHANGE),
                eq(VERIFY_ROUTING_KEY),
                any(EmailRequest.class)
        );
    }

    @Test
    @DisplayName("Should verify change email token")
    void verifyChangeEmail_ShouldUpdateUserEmailAndSendNewVerification() {
        // Given
        ChangeEmail changeEmail = new ChangeEmail(1L, mockUser, "new@test.com", LocalDateTime.now(), mockToken);
        when(confirmationTokenRepository.findByToken("sample-token")).thenReturn(Optional.of(mockToken));
        when(changeEmailRepository.findByConfirmationToken(mockToken)).thenReturn(Optional.of(changeEmail));

        // When
        String url = confirmationTokenService.verifyChangeEmail("sample-token");

        // Then
        assertEquals(website + "/login", url);
        assertEquals("new@test.com", mockUser.getEmail());
        verify(appUserRepository).save(mockUser);
        verify(rabbitTemplate).convertAndSend(
                eq(EMAIL_EXCHANGE),
                eq(VERIFY_ROUTING_KEY),
                any(EmailRequest.class)
        );
    }
}
