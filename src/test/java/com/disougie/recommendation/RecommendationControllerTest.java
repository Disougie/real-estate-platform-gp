package com.disougie.recommendation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;

@WebMvcTest(
    value = RecommendationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    @DisplayName("GET /api/v1/recommendation - Should return 200 and list of recommended properties")
    void getRecommendations_ShouldReturn200() throws Exception {
        // Given
        PropertyDetailedResponse response = new PropertyDetailedResponse(
                "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
                );
        when(recommendationService.getRecommendations()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/recommendation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("prop-1"));
    }
}
