package com.disougie.app_user.forgot_password;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = ResetPasswordController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class ResetPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordService passwordService;

    @Test
    @DisplayName("GET /api/v1/reset-password - Should return reset page with token attribute")
    void resetPage_ShouldReturnResetView() throws Exception {
        mockMvc.perform(get("/api/v1/reset-password")
                .param("token", "sample-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("token", "sample-token"));
    }

    @Test
    @DisplayName("POST /api/v1/reset-password - Should reset password and redirect")
    void resetPassword_ShouldReturn302Redirect() throws Exception {
        // Given
        String redirectUrl = "http://localhost:3000/login";
        when(passwordService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(redirectUrl);

        // When & Then
        // Request parameters are passed as form-data since @RequestBody is commented out in controller
        mockMvc.perform(post("/api/v1/reset-password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("token", "sample-token")
                .param("newPassword", "newPass123")
                .param("confirmPassword", "newPass123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUrl));
    }
}
