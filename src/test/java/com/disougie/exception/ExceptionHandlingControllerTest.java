package com.disougie.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@WebMvcTest(
    value = {ExceptionHandlingControllerTest.TestController.class, ExceptionHandlingController.class},
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class ExceptionHandlingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404")
    void handleResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException and return 400")
    void handleConstraintViolation() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Constraint violation"));
    }

    @Test
    @DisplayName("Should handle AuthenticationException and return 401")
    void handleAuthentication() throws Exception {
        mockMvc.perform(get("/test/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    @DisplayName("Should handle AccessDeniedException and return 403")
    void handleAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Should handle MailSendException and return 500")
    void handleMailSend() throws Exception {
        mockMvc.perform(get("/test/mail-send"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("can't send emails now try resend token again later"));
    }

    @Test
    @DisplayName("Should handle general Exception and return 500")
    void handleGeneralException() throws Exception {
        mockMvc.perform(get("/test/general"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server Error"));
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException and return 400")
    void handleMethodNotSupported() throws Exception {
        mockMvc.perform(post("/test/resource-not-found")) // Uses POST instead of GET
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400")
    void handleMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException and return 400")
    void handleMessageNotReadable() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Http request Not Readable"));
    }

    @RestController
    static class TestController {
        
        @GetMapping("/test/resource-not-found")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/test/constraint-violation")
        public void throwConstraintViolation() {
            throw new ConstraintViolationException("Constraint violation", Set.of());
        }

        @GetMapping("/test/authentication")
        public void throwAuthentication() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/test/mail-send")
        public void throwMailSend() {
            throw new MailSendException("Mail send failed");
        }

        @GetMapping("/test/general")
        public void throwGeneral() throws Exception {
            throw new Exception("General error");
        }

        @PostMapping("/test/validation")
        public void testValidation(@Valid @RequestBody TestDto testDto) {
        }
    }

    @Data
    static class TestDto {
        @NotBlank(message = "must not be blank")
        private String field;
    }
}
