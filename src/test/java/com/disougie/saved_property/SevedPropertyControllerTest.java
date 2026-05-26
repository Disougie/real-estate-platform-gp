package com.disougie.saved_property;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    value = SevedPropertyController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class SevedPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedPropertyService savedPropertyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/saved-properties - Should return list of saved properties")
    void getMySavedProperties_ShouldReturn200() throws Exception {
        // Given
        PropertyDetailedResponse response1 = new PropertyDetailedResponse(
                "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
                );
        when(savedPropertyService.getMySavedProperties()).thenReturn(List.of(response1));

        // When & Then
        mockMvc.perform(get("/api/v1/saved-properties")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("prop-1"))
                .andExpect(jsonPath("$[0].title").value("Title 1"));
    }

    @Test
    @DisplayName("POST /api/v1/saved-properties - Should save property")
    void saveProperty_ShouldReturn201() throws Exception {
        // Given
        SavePropertyRequest request = new SavePropertyRequest("prop-1");
        doNothing().when(savedPropertyService).saveProperty(any(SavePropertyRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/saved-properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/v1/saved-properties/{id} - Should remove property from saved list")
    void removeFromSaved_ShouldReturn204() throws Exception {
        // Given
        String propertyId = "prop-1";
        doNothing().when(savedPropertyService).removeFromSavedProperties(propertyId);

        // When & Then
        mockMvc.perform(delete("/api/v1/saved-properties/{id}", propertyId))
                .andExpect(status().isNoContent());
    }
}
