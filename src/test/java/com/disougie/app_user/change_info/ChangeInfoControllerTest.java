package com.disougie.app_user.change_info;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.disougie.config.TestConfig;
import com.disougie.redis.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    value = ChangeInfoController.class,
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
public class ChangeInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChangeInfoService changeInfoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/change/email - Should change email and return 204")
    void changeEmail_ShouldReturn204() throws Exception {
        // Given
        ChangeEmailRequest request = new ChangeEmailRequest("new@example.com");
        doNothing().when(changeInfoService).changeEmail(any(ChangeEmailRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/change/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/change/password - Should change password and return 204")
    void changePassword_ShouldReturn204() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");
        doNothing().when(changeInfoService).changePassword(any(ChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/change/phone - Should change phone and return 204")
    void changePhone_ShouldReturn204() throws Exception {
        // Given
        ChangePhoneRequest request = new ChangePhoneRequest("123456789", "password123");
        doNothing().when(changeInfoService).changePhone(any(ChangePhoneRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/change/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
