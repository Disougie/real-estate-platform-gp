package com.disougie.app_user.confirmation_token;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    value = ConfirmationTokenController.class,
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
public class ConfirmationTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfirmationTokenService tokenService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/token/verify - Should verify token and redirect")
    void verifyToken_ShouldReturn302Redirect() throws Exception {
        // Given
        String token = "sample-token";
        String redirectUrl = "http://localhost:3000/login";
        when(tokenService.verifyToken(token)).thenReturn(redirectUrl);

        // When & Then
        mockMvc.perform(get("/api/v1/token/verify")
                .param("token", token))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUrl));
    }

    @Test
    @DisplayName("POST /api/v1/token/resend - Should resend token")
    void resendToken_ShouldReturn204() throws Exception {
        // Given
        ResendTokenRequest request = new ResendTokenRequest("user@test.com");
        doNothing().when(tokenService).resendToken(any(ResendTokenRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/token/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/token/verify-change - Should verify change email token and redirect")
    void verifyChangeEmail_ShouldReturn302Redirect() throws Exception {
        // Given
        String token = "change-email-token";
        String redirectUrl = "http://localhost:3000/login";
        when(tokenService.verifyChangeEmail(token)).thenReturn(redirectUrl);

        // When & Then
        mockMvc.perform(get("/api/v1/token/verify-change")
                .param("token", token))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUrl));
    }
}
