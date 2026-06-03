package com.disougie.property;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import com.disougie.app_user.AppUser;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.imagekit.ImageService;
import com.disougie.property.entity.Features;
import com.disougie.property.entity.Location;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.recommendation.RecommendationRequest;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTest {

    @Mock
    private PropertyDetailedResponseMapper propertyDetailedResponseMapper;
    @Mock
    private PropertyBriefResponseMapper propertyBriefResponseMapper;
    @Mock
    private PropertyMapResponseMapper propertyMapResponseMapper;
    @SuppressWarnings("rawtypes")
	@Mock
    private PageResponseMapper pageResponseMapper;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PropertyService propertyService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser mockUser;
    private Property mockProperty;

    @BeforeEach
    void setUp() {
        mockUser = AppUser.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        mockProperty = Property.builder()
                .id("prop-1")
                .ownerId(1L)
                .title("Test Property")
                .price(1000.0)
                .type(PropertyType.RENT)
                .status(PropertyStatus.AVAILABLE)
                .location(new Location("Cairo", "Maadi"))
                .features(new Features(2, 1, 100))
                .build();

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should return page of properties successfully")
    @SuppressWarnings("unchecked")
    void getProperties_ShouldReturnPageOfProperties() {
        // Given
        int page = 0;
        int size = 10;
        Page<Property> propertyPage = new PageImpl<>(List.of(mockProperty));
        
        when(propertyRepository.findAllApprovedProperties(PageRequest.of(page, size)))
                .thenReturn(propertyPage);
        
        PageResponse<PropertyBriefResponse> expectedResponse = new PageResponse<>(List.of(), 0,0,0,0,true);
        when(pageResponseMapper.apply(any())).thenReturn(expectedResponse);

        // When
        PageResponse<PropertyBriefResponse> result = propertyService.getProperties(page, size);

        // Then
        assertNotNull(result);
        verify(propertyRepository).findAllApprovedProperties(PageRequest.of(page, size));
    }

    @Test
    @DisplayName("Should get property by ID and send recommendation message")
    void getProperty_ShouldReturnPropertyAndSendMessage() {
        // Given
        String propertyId = "prop-1";
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));
        
        PropertyDetailedResponse expectedResponse = new PropertyDetailedResponse(
                "prop-1", "Test Property", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
        );
        when(propertyDetailedResponseMapper.apply(mockProperty)).thenReturn(expectedResponse);

        // When
        PropertyDetailedResponse result = propertyService.getProperty(propertyId);

        // Then
        assertNotNull(result);
        assertEquals("Test Property", result.title());
        verify(rabbitTemplate).convertAndSend(
                eq(RECOMMENDATION_EXCHANGE),
                eq(RECOMMENDATION_ROUTING_KEY),
                any(RecommendationRequest.class)
        );
    }

    @Test
    @DisplayName("Should throw exception if property not found by ID")
    void getProperty_ShouldThrowResourceNotFoundException() {
        // Given
        String propertyId = "invalid-id";
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> propertyService.getProperty(propertyId));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(RecommendationRequest.class));
    }

    @Test
    @DisplayName("Should allow owner to view their property")
    void getMyProperty_ShouldReturnPropertyForOwner() {
        // Given
        String propertyId = "prop-1";
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));
        
        PropertyDetailedResponse expectedResponse = new PropertyDetailedResponse(
                "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
        );
        when(propertyDetailedResponseMapper.apply(mockProperty)).thenReturn(expectedResponse);

        // When
        PropertyDetailedResponse result = propertyService.getMyProperty(propertyId);

        // Then
        assertNotNull(result);
        verify(propertyDetailedResponseMapper).apply(mockProperty);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when trying to view someone else's property")
    void getMyProperty_ShouldThrowAccessDeniedExceptionForNonOwner() {
        // Given
        String propertyId = "prop-1";
        AppUser otherUser = AppUser.builder().id(99L).build();
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(otherUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> propertyService.getMyProperty(propertyId));
    }

    @Test
    @DisplayName("Should add new property ad and return its ID")
    void addPropertyAd_ShouldSavePropertyAndReturnId() {
        // Given
        PropertyAdPostRequest request = new PropertyAdPostRequest(
                "Title", 1500.0, PropertyType.RENT,
                30.0, 31.0, "Description", 2, 1, null,"Cairo", "Maadi", 100
            );
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        
        Property savedProperty = Property.builder().id("prop-new").build();
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        // When
        PropertyAdCreationResponse result = propertyService.addPropertyAd(request);

        // Then
        assertNotNull(result);
        assertEquals("prop-new", result.property_id());
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    @DisplayName("Should successfully delete property ad if user is the owner")
    void deletePropertyAd_ShouldDeleteIfOwner() {
        // Given
        String propertyId = "prop-1";
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));

        // When
        propertyService.deletePropertyAd(propertyId);

        // Then
        verify(propertyRepository).delete(mockProperty);
    }
    
    @Test
    @DisplayName("Should throw AccessDeniedException when non-owner tries to delete property")
    void deletePropertyAd_ShouldThrowAccessDeniedExceptionForNonOwner() {
        // Given
        String propertyId = "prop-1";
        AppUser otherUser = AppUser.builder().id(99L).build();
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(otherUser);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> propertyService.deletePropertyAd(propertyId));
        verify(propertyRepository, never()).delete(any(Property.class));
    }

    @Test
    @DisplayName("Should successfully add a review if property is AVAILABLE")
    void ratePropertyAd_ShouldSaveReviewIfPropertyIsAvailable() {
        // Given
        String propertyId = "prop-1";
        ReviewRequest reviewRequest = new ReviewRequest(5.0);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));

        // When
        propertyService.ratePropertyAd(propertyId, reviewRequest);

        // Then
        assertNotNull(mockProperty.getReview());
        verify(propertyRepository).save(mockProperty);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if trying to rate a non-AVAILABLE property")
    void ratePropertyAd_ShouldThrowAccessDeniedExceptionIfNotAvailable() {
        // Given
        String propertyId = "prop-1";
        mockProperty.setStatus(PropertyStatus.BANNED);
        ReviewRequest reviewRequest = new ReviewRequest(5.0);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(mockProperty));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> propertyService.ratePropertyAd(propertyId, reviewRequest));
        verify(propertyRepository, never()).save(any(Property.class));
    }
}
