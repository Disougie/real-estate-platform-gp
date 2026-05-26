package com.disougie.app_user.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.AppUserResponse;
import com.disougie.app_user.AppUserResponseMapper;
import com.disougie.app_user.AppUserRole;
import com.disougie.app_user.registration.RegistrationRequest;
import com.disougie.app_user.registration.RegistrationResponse;
import com.disougie.app_user.registration.RegistrationService;
import com.disougie.blog.Blog;
import com.disougie.blog.BlogCreationResponse;
import com.disougie.blog.BlogRepository;
import com.disougie.blog.BlogRequest;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.property.PropertyAdminResponseMapper;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private RegistrationService registrationService;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private AppUserResponseMapper appUserResponseMapper;
    @Mock
    private PropertyAdminResponseMapper propertyAdminResponseMapper;
    @SuppressWarnings("rawtypes")
	@Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private AdminService adminService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser mockAdmin;
    private AppUser mockUser;
    private Property mockProperty;
    private Blog mockBlog;

    @BeforeEach
    void setUp() {
        mockAdmin = AppUser.builder().id(1L).name("Admin").role(AppUserRole.ADMIN).build();
        mockUser = AppUser.builder().id(2L).name("User").role(AppUserRole.USER).build();
        mockProperty = Property.builder().id("prop-1").title("Villa").build();
        
        mockBlog = new Blog();
        mockBlog.setId(10L);
        mockBlog.setTitle("Blog Title");
        mockBlog.setContent("Content");
        mockBlog.setWriter(mockAdmin);
        mockBlog.setCreatedAt(LocalDateTime.now());
        
        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should get app users by role")
    @SuppressWarnings("unchecked")
    void getAppUsers_ShouldReturnPageOfUsers() {
        // Given
        Page<AppUser> userPage = new PageImpl<>(List.of(mockUser));
        when(appUserRepository.findByRoleForAdmin(PageRequest.of(0, 10), "USER")).thenReturn(userPage);
        when(pageResponseMapper.apply(any())).thenReturn(new PageResponse<>(List.of(), 0, 0, 0, 0, true));

        // When
        PageResponse<AppUserResponse> response = adminService.getAppUsers(AppUserRole.USER, 0, 10);

        // Then
        assertNotNull(response);
        verify(appUserRepository).findByRoleForAdmin(PageRequest.of(0, 10), "USER");
    }

    @Test
    @DisplayName("Should delete an app user")
    void deleteAppUser_ShouldDeleteUser() {
        // Given
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(mockUser));

        // When
        adminService.deleteAppUser(2L);

        // Then
        verify(appUserRepository).delete(mockUser);
    }

    @Test
    @DisplayName("Should register a new app user")
    void registerAppUser_ShouldCallRegistrationService() {
        // Given
        RegistrationRequest request = new RegistrationRequest("Lawyer", "lawyer@test.com", "pass", "pass", "123");
        RegistrationResponse expectedResponse = new RegistrationResponse(3L);
        when(registrationService.registerUser(request, AppUserRole.LAWYER)).thenReturn(expectedResponse);

        // When
        RegistrationResponse result = adminService.registerAppUser(request, AppUserRole.LAWYER);

        // Then
        assertEquals(3L, result.id());
        verify(registrationService).registerUser(request, AppUserRole.LAWYER);
    }

    @Test
    @DisplayName("Should delete a property")
    void deleteProperty_ShouldDeleteProperty() {
        // Given
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(mockProperty));

        // When
        adminService.deleteProperty("prop-1");

        // Then
        verify(propertyRepository).delete(mockProperty);
    }

    @Test
    @DisplayName("Should add a blog")
    void addBlog_ShouldSaveAndReturnBlogCreationResponse() {
        // Given
        BlogRequest request = new BlogRequest("New Blog", "Content here");
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockAdmin);
        when(blogRepository.save(any(Blog.class))).thenReturn(mockBlog);

        // When
        BlogCreationResponse response = adminService.addBlog(request);

        // Then
        assertNotNull(response);
        assertEquals(10L, response.id());
        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    @DisplayName("Should update an existing blog")
    void updateBlog_ShouldUpdateTitleAndContent() {
        // Given
        BlogRequest request = new BlogRequest("Updated Title", "Updated Content");
        when(blogRepository.findById(10L)).thenReturn(Optional.of(mockBlog));

        // When
        adminService.updateBlog(10L, request);

        // Then
        assertEquals("Updated Title", mockBlog.getTitle());
        assertEquals("Updated Content", mockBlog.getContent());
        verify(blogRepository).save(mockBlog);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent blog")
    void deleteBlog_ShouldThrowException() {
        // Given
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteBlog(99L));
    }
}
