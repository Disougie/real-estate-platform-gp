package com.disougie.app_user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.security.JwtService;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class DisableUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private DisableUserService disableUserService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser mockUser;
    private DisableRequest request;

    @BeforeEach
    void setUp() {
        mockUser = AppUser.builder()
                .id(1L)
                .email("user@example.com")
                .password("encodedPassword")
                .build();
                
        request = new DisableRequest("plainPassword");
        
        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should successfully disable account and delete all owned properties")
    void disableAccount_ShouldDeleteUserAndProperties() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        
        Property mockProperty = Property.builder().id("prop-1").build();
        when(propertyRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(mockProperty));

        // When
        disableUserService.disableAccount(request);

        // Then
        verify(propertyRepository).delete(mockProperty);
        verify(appUserRepository).delete(mockUser);
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if user is main admin")
    void disableAccount_ShouldThrowExceptionForMainAdmin() {
        // Given
        AppUser adminUser = AppUser.builder().email("admin@system.com").build();
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(adminUser);

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> disableUserService.disableAccount(request));
        
        assertTrue(exception.getMessage().contains("admin can not be disabled"));
        verify(appUserRepository, never()).delete(any(AppUser.class));
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if password does not match")
    void disableAccount_ShouldThrowExceptionForIncorrectPassword() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> disableUserService.disableAccount(request));
        
        assertTrue(exception.getMessage().contains("password not correct"));
        verify(propertyRepository, never()).findByOwnerId(any(Long.class));
        verify(appUserRepository, never()).delete(any(AppUser.class));
    }
}
