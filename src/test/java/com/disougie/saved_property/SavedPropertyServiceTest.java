package com.disougie.saved_property;

import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_EXCHANGE;
import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.disougie.app_user.AppUser;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Features;
import com.disougie.property.entity.Location;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.recommendation.RecommendationRequest;
import com.disougie.security.JwtService;

@ExtendWith(MockitoExtension.class)
public class SavedPropertyServiceTest {

    @Mock
    private SavedPropertyRepository savedPropertyRepository;

    @Mock
    private SavedPropertyResponseMapper savedPropertyResponseMapper;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SavedPropertyService savedPropertyService;

    private MockedStatic<JwtService> mockedJwtService;
    private AppUser mockUser;
    private SavedProperty mockSavedProperty;
    private Property mockProperty;

    @BeforeEach
    void setUp() {
        mockUser = AppUser.builder()
                .id(1L)
                .email("user@test.com")
                .build();
                
        mockSavedProperty = new SavedProperty(new SavedPropertyId(1L, "prop-1"), mockUser);
        
        mockProperty = Property.builder()
                .id("prop-1")
                .location(new Location("Cairo", "Maadi"))
                .type(PropertyType.PURCHASE)
                .price(100000.0)
                .features(new Features(3, 2, 150))
                .build();

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should get all saved properties for the current user")
    void getMySavedProperties_ShouldReturnListOfDetailedResponses() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(savedPropertyRepository.findByUser(mockUser)).thenReturn(List.of(mockSavedProperty));
        
        PropertyDetailedResponse expectedResponse = new PropertyDetailedResponse(
                "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
                );
        when(savedPropertyResponseMapper.apply(mockSavedProperty)).thenReturn(expectedResponse);

        // When
        List<PropertyDetailedResponse> result = savedPropertyService.getMySavedProperties();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prop-1", result.get(0).id());
        verify(savedPropertyRepository).findByUser(mockUser);
    }

    @Test
    @DisplayName("Should save property and send recommendation message")
    void saveProperty_ShouldSaveAndSendMessage() {
        // Given
        SavePropertyRequest request = new SavePropertyRequest("prop-1");
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(mockProperty));

        // When
        savedPropertyService.saveProperty(request);

        // Then
        verify(savedPropertyRepository).save(any(SavedProperty.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RECOMMENDATION_EXCHANGE),
                eq(RECOMMENDATION_ROUTING_KEY),
                any(RecommendationRequest.class)
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when saving non-existent property")
    void saveProperty_ShouldThrowExceptionWhenPropertyNotFound() {
        // Given
        SavePropertyRequest request = new SavePropertyRequest("invalid-prop");
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById("invalid-prop")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> savedPropertyService.saveProperty(request));
        verify(savedPropertyRepository, never()).save(any(SavedProperty.class));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(RecommendationRequest.class));
    }

    @Test
    @DisplayName("Should remove property from saved properties")
    void removeFromSavedProperties_ShouldDeleteById() {
        // Given
        String propertyId = "prop-1";
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);

        // When
        savedPropertyService.removeFromSavedProperties(propertyId);

        // Then
        verify(savedPropertyRepository).deleteById(new SavedPropertyId(mockUser.getId(), propertyId));
    }
}
