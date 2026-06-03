package com.disougie.app_user.admin;

import static com.disougie.app_user.AppUserRole.ADMIN;
import static com.disougie.app_user.AppUserRole.LAWYER;
import static com.disougie.app_user.AppUserRole.USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.disougie.app_user.registration.RegistrationRequest;
import com.disougie.app_user.registration.RegistrationResponse;
import com.disougie.blog.BlogCreationResponse;
import com.disougie.blog.BlogRequest;
import com.disougie.config.TestConfig;
import com.disougie.redis.RateLimitFilter;
import com.disougie.util.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    value = AdminController.class,
    excludeAutoConfiguration = {
    		SecurityAutoConfiguration.class, 
    	    UserDetailsServiceAutoConfiguration.class
    },
	excludeFilters = {
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class),
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class) // استبعاد الفلتر
	}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private ObjectMapper objectMapper = new ObjectMapper();

	@Test
    @DisplayName("GET /api/v1/admin/users - Should return list of users")
    void getUsers_ShouldReturn200() throws Exception {
        when(adminService.getAppUsers(eq(USER), anyInt(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 0, 0, 0, true));
        mockMvc.perform(get("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/admin/lawyers - Should register lawyer")
    void registerLawyer_ShouldReturn200() throws Exception {
        RegistrationRequest request = new RegistrationRequest("Lawyer", "0123456789", "lawyer@test.com", "password", "password");
        when(adminService.registerAppUser(any(RegistrationRequest.class), eq(LAWYER)))
                .thenReturn(new RegistrationResponse(2L));

        mockMvc.perform(post("/api/v1/admin/lawyers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @DisplayName("POST /api/v1/admin/admins - Should register admin")
    void registerAdmin_ShouldReturn200() throws Exception {
        RegistrationRequest request = new RegistrationRequest("Admin", "0123456789", "admin@test.com", "password", "password");
        when(adminService.registerAppUser(any(RegistrationRequest.class), eq(ADMIN)))
                .thenReturn(new RegistrationResponse(3L));

        mockMvc.perform(post("/api/v1/admin/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/users/{id} - Should delete user")
    void deleteUser_ShouldReturn200() throws Exception {
        doNothing().when(adminService).deleteAppUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/admin/properties - Should return properties")
    void getProperties_ShouldReturn200() throws Exception {
        when(adminService.getProperties(anyInt(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 0, 0, 0, true));

        mockMvc.perform(get("/api/v1/admin/properties")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/properties/{id} - Should delete property")
    void deleteProperty_ShouldReturn204() throws Exception {
        doNothing().when(adminService).deleteProperty("prop-1");

        mockMvc.perform(delete("/api/v1/admin/properties/{id}", "prop-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/admin/blogs - Should add new blog")
    void addBlog_ShouldReturn200() throws Exception {
        BlogRequest request = new BlogRequest("Title", "Content");
        when(adminService.addBlog(any(BlogRequest.class))).thenReturn(new BlogCreationResponse(10L));

        mockMvc.perform(post("/api/v1/admin/blogs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/blogs/{id} - Should update blog")
    void updateBlog_ShouldReturn204() throws Exception {
        BlogRequest request = new BlogRequest("Updated Title", "Updated Content");
        doNothing().when(adminService).updateBlog(eq(10L), any(BlogRequest.class));

        mockMvc.perform(patch("/api/v1/admin/blogs/{id}", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
