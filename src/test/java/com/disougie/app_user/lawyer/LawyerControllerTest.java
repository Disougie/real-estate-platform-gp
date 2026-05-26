package com.disougie.app_user.lawyer;

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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.disougie.intial_contract.initialContractResponse;

@WebMvcTest(
    value = LawyerController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class LawyerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LawyerService lawyerService;

    @Test
    @DisplayName("GET /api/v1/lawyer/initial-contracts - Should return pending contracts")
    void getPendingContracts_ShouldReturn200() throws Exception {
        // Given
        initialContractResponse response = new initialContractResponse(1L, null, null, null, null, null, null, null);
        when(lawyerService.getPendingContracts()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/lawyer/initial-contracts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/lawyer/initial-contracts/{id} - Should return contract by id")
    void getContract_ShouldReturn200() throws Exception {
        // Given
        initialContractResponse response = new initialContractResponse(10L, null, null, null, null, null, null, null);
        when(lawyerService.getContract(10L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/lawyer/initial-contracts/{id}", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("POST /api/v1/lawyer/initial-contracts/{id}/working-on - Should assign lawyer")
    void workingOnContract_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(lawyerService).workingOnContract(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/lawyer/initial-contracts/{id}/working-on", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/lawyer/initial-contracts/{id}/complete - Should complete contract")
    void completeContract_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(lawyerService).completeContract(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/lawyer/initial-contracts/{id}/complete", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
