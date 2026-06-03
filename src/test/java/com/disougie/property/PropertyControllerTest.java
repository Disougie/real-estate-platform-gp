package com.disougie.property;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.redis.RateLimitFilter;
import com.disougie.util.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    value = PropertyController.class,
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
public class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/properties - Should return page of properties (Happy Path)")
    void getProperties_ShouldReturn200() throws Exception {
        // Given
        PageResponse<PropertyBriefResponse> pageResponse = new PageResponse<>(List.of(), 0, 0, 0, 0,true);
        when(propertyService.getProperties(0, 20)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/properties")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/properties/{id} - Should return property details")
    void getProperty_ShouldReturn200() throws Exception {
        // Given
        String propertyId = "prop-1";
        PropertyDetailedResponse response = new PropertyDetailedResponse(
            "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
        );
        when(propertyService.getProperty(propertyId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/properties/{id}", propertyId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prop-1"))
                .andExpect(jsonPath("$.title").value("Villa"));
    }

    @Test
    @DisplayName("POST /api/v1/properties - Should create a new property ad (Happy Path)")
    void addPropertyAd_ShouldReturn201() throws Exception {
        // Given
        PropertyAdPostRequest request = new PropertyAdPostRequest(
            "Title", 1500.0, PropertyType.RENT,
            30.0, 31.0, "Description", 2, 1, null,"Cairo", "Maadi", 100
        );
        PropertyAdCreationResponse response = new PropertyAdCreationResponse("new-id");
        
        when(propertyService.addPropertyAd(any(PropertyAdPostRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.property_id").value("new-id"));
    }

    @Test
    @DisplayName("POST /api/v1/properties - Should return 400 when request body is invalid")
    void addPropertyAd_ShouldReturn400WhenInvalid() throws Exception {
        // Given (missing required fields in request to simulate validation failure if @Validated is enforced)
        String invalidPayload = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/properties/{id} - Should delete property successfully")
    void deletePropertyAd_ShouldReturn204() throws Exception {
        // Given
        String propertyId = "prop-1";
        doNothing().when(propertyService).deletePropertyAd(propertyId);

        // When & Then
        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/properties/search - Should search by text")
    void searchByText_ShouldReturn200() throws Exception {
        // Given
        String text = "Villa";
        PageResponse<PropertyBriefResponse> pageResponse = new PageResponse<>(List.of(), 0,0,0,0,true);
        when(propertyService.searchByText(text, 0, 20)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/properties/search")
                .param("text", text)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("POST /api/v1/properties/rating/{id} - Should rate property ad")
    void ratePropertyAd_ShouldReturn204() throws Exception {
        // Given
        String propertyId = "prop-1";
        ReviewRequest request = new ReviewRequest(4.5);
        doNothing().when(propertyService).ratePropertyAd(eq(propertyId), any(ReviewRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/properties/rating/{id}", propertyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
