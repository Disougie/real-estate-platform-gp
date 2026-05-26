package com.disougie.app_user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.disougie.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserResponseMapper appUserResponseMapper;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = AppUser.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(AppUserRole.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully load user by username (email)")
    void loadUserByUsername_ShouldReturnUserDetails() {
        // Given
        when(appUserRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));

        // When
        UserDetails userDetails = appUserService.loadUserByUsername("john@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("john@example.com", userDetails.getUsername());
        verify(appUserRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when loading non-existent user")
    void loadUserByUsername_ShouldThrowExceptionForInvalidEmail() {
        // Given
        when(appUserRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> appUserService.loadUserByUsername("invalid@example.com"));
        verify(appUserRepository).findByEmail("invalid@example.com");
    }

    @Test
    @DisplayName("Should successfully get user by ID and return mapped response")
    void getUser_ShouldReturnAppUserResponse() {
        // Given
        Long userId = 1L;
        AppUserResponse expectedResponse = new AppUserResponse(1L, "John Doe", "john@example.com", "123456789", true, null);
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(appUserResponseMapper.apply(mockUser)).thenReturn(expectedResponse);

        // When
        AppUserResponse result = appUserService.getUser(userId);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("john@example.com", result.email());
        verify(appUserRepository).findById(userId);
        verify(appUserResponseMapper).apply(mockUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user ID does not exist")
    void getUser_ShouldThrowExceptionForInvalidId() {
        // Given
        Long invalidId = 99L;
        when(appUserRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> appUserService.getUser(invalidId));
        verify(appUserRepository).findById(invalidId);
    }
}
