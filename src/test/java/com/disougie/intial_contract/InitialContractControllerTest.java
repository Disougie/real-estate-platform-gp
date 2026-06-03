package com.disougie.intial_contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.disougie.config.TestConfig;
import com.disougie.redis.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    value = InitialContractController.class,
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
public class InitialContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InitialContractService initialContractService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/initial-contracts - Should return 200 and list of contracts")
    void getMyInitialContracts_ShouldReturn200() throws Exception {
        // Given
        initialContractResponse response = new initialContractResponse(1L, null, null, null, null, null, null, null);
        when(initialContractService.getMyInitialContracts()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/initial-contracts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/initial-contracts/{id} - Should return 200 and contract by id")
    void getInitialContract_ShouldReturn200() throws Exception {
        // Given
        initialContractResponse response = new initialContractResponse(10L, null, null, null, null, null, null, null);
        when(initialContractService.getInitialContract(10L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/initial-contracts/{id}", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("POST /api/v1/initial-contracts - Should create contract and return 200")
    void createInitialContract_ShouldReturn200() throws Exception {
        // Given
        InitialContractCreationRequest request = new InitialContractCreationRequest("prop-1", 12);
        InitialContractCreationResponse response = new InitialContractCreationResponse(10L);
        when(initialContractService.createInitialContract(any(InitialContractCreationRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/initial-contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("POST /api/v1/initial-contracts/{id}/accept - Should accept contract and return 204")
    void acceptContract_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(initialContractService).acceptContract(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/initial-contracts/{id}/accept", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/initial-contracts/{id}/reject - Should reject contract and return 204")
    void rejectContract_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(initialContractService).rejectContract(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/initial-contracts/{id}/reject", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
