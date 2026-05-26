package com.disougie.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private AppUser mockUser;
    private final String secretKeyString = "my-super-secret-key-my-super-secret-key"; // >= 32 chars for HMAC-SHA
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "Key", secretKeyString);

        mockUser = AppUser.builder()
                .email("test@example.com")
                .role(AppUserRole.USER)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null && !mockedSecurityContextHolder.isClosed()) {
            mockedSecurityContextHolder.close();
        }
    }

    @Test
    @DisplayName("Should retrieve current user from SecurityContext")
    void getCurrentUser_ShouldReturnAppUser() {
        // Given
        SecurityContext securityContext = mock(SecurityContext.class);
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // When
        AppUser result = JwtService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void generateJwtToken_ShouldReturnTokenWithBearerPrefix() {
        // Given
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // When
        String token = jwtService.generateJwtToken(authentication);

        // Then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
        
        // Ensure we can extract the username from it
        String extractedUsername = jwtService.extractUsername(token.substring(7));
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    @DisplayName("Should successfully validate a valid token")
    void isValidJwtToken_ShouldReturnTrueForValidToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(mockUser);
        String token = jwtService.generateJwtToken(authentication).substring(7); // Remove "Bearer "

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // When
        boolean isValid = jwtService.isValidJwtToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should throw Exception when validating token for different user")
    void isValidJwtToken_ShouldThrowExceptionForMismatchedUsername() {
        // Given
        when(authentication.getPrincipal()).thenReturn(mockUser);
        String token = jwtService.generateJwtToken(authentication).substring(7);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("different@example.com");

        // When & Then
        assertThrows(org.springframework.security.core.AuthenticationException.class, 
                () -> jwtService.isValidJwtToken(token, userDetails));
    }

    @Test
    @DisplayName("Should throw Exception when validating expired token")
    void isValidJwtToken_ShouldThrowExceptionForExpiredToken() {
        // Generate an expired token manually
        Date issuedAt = Date.from(LocalDateTime.now().minusMinutes(60).atZone(ZoneId.systemDefault()).toInstant());
        Date expiration = Date.from(LocalDateTime.now().minusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
        
        String expiredToken = Jwts.builder()
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject("test@example.com")
                .signWith(jwtService.secretKey())
                .compact();

        UserDetails userDetails = mock(UserDetails.class);

        // When & Then
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, 
                () -> jwtService.isValidJwtToken(expiredToken, userDetails));
    }
}
