package com.disougie.app_user.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;
import com.disougie.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private LoginService loginService;

    private LoginRequest loginRequest;
    private AppUser authenticatedUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("user@example.com", "password123");
        
        authenticatedUser = AppUser.builder()
                .id(1L)
                .name("Test User")
                .email("user@example.com")
                .phone("123456")
                .role(AppUserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Should login successfully, generate JWT and set response header")
    void login_ShouldReturnLoginResponseAndSetHeader() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(jwtService.generateJwtToken(authentication)).thenReturn("sample-jwt-token");

        // When
        LoginResponse response = loginService.login(loginRequest, httpServletResponse);

        // Then
        assertNotNull(response);
        assertEquals("Test User", response.name());
        assertEquals("USER", response.role());
        assertEquals(1L, response.id());
        
        verify(httpServletResponse).setHeader("Authorization", "sample-jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateJwtToken(authentication);
    }

    @Test
    @DisplayName("Should throw Exception when authentication fails")
    void login_ShouldThrowExceptionForBadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> loginService.login(loginRequest, httpServletResponse));
        verify(jwtService, org.mockito.Mockito.never()).generateJwtToken(any(Authentication.class));
    }
}
