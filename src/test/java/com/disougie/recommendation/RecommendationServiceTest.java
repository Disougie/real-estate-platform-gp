package com.disougie.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.disougie.app_user.AppUser;
import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.PropertyDetailedResponseMapper;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Features;
import com.disougie.property.entity.Location;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.security.JwtService;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PropertyDetailedResponseMapper propertyDetailedResponseMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Channel channel;

    @InjectMocks
    private RecommendationService recommendationService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser user;
    private UserPreference userPreference;
    private Property property;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().id(1L).email("user@test.com").build();
        
        Map<String, Integer> cityMap = new HashMap<>();
        cityMap.put("Cairo", 5);
        Map<String, Integer> areaMap = new HashMap<>();
        areaMap.put("Maadi", 3);
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("RENT", 2);

        userPreference = UserPreference.builder()
                .userId(1L)
                .preferencedCity(cityMap)
                .preferencedArea(areaMap)
                .preferencedType(typeMap)
                .preferencedPrice(5000.0)
                .preferencedSize(120)
                .preferencedCont(4) // One more will trigger compute
                .build();

        Location location = new Location("Cairo", "Maadi");
        Features features = new Features(3, 2, 120);
        property = Property.builder()
                .id("prop-1")
                .location(location)
                .features(features)
                .type(com.disougie.property.entity.PropertyType.RENT)
                .price(4800.0)
                .build();

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should create new preferences and ack message if preference not found")
    void updateUserPreferences_ShouldCreateNewPreference() throws IOException {
        // Given
        RecommendationRequest request = new RecommendationRequest(1L, "Cairo", "Maadi", "RENT", 5000.0, 120, EventType.VIEW);
        Message message = mock(Message.class);
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(123L);
        when(message.getMessageProperties()).thenReturn(properties);
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        recommendationService.updateUserPreferences(request, message, channel);

        // Then
        verify(userPreferenceRepository).save(any(UserPreference.class));
        verify(channel).basicAck(123L, false);
    }

    @Test
    @DisplayName("Should update existing preferences and compute recommendations if count % 5 == 0")
    void updateUserPreferences_ShouldUpdateAndCompute() throws IOException {
        // Given
        RecommendationRequest request = new RecommendationRequest(1L, "Cairo", "Maadi", "RENT", 5500.0, 130, EventType.FAVOURITE);
        Message message = mock(Message.class);
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(456L);
        when(message.getMessageProperties()).thenReturn(properties);
        
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(propertyRepository.findUserPrefrencePropertyies(anyCollection(), anyCollection(), anyCollection(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(List.of(property));

        // When
        recommendationService.updateUserPreferences(request, message, channel);

        // Then
        verify(userPreferenceRepository).save(userPreference);
        assertEquals(5, userPreference.getPreferencedCont());
        verify(propertyRepository).findUserPrefrencePropertyies(anyCollection(), anyCollection(), anyCollection(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(Pageable.class));
        verify(valueOperations).set(eq("recommendation:user:1"), any(List.class), eq(15L), eq(TimeUnit.MINUTES));
        verify(channel).basicAck(456L, false);
    }

    @Test
    @DisplayName("Should return empty list if user has no recommendations in redis and no preference")
    void getRecommendations_ShouldReturnEmptyList() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("recommendation:user:1")).thenReturn(null);
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        List<PropertyDetailedResponse> result = recommendationService.getRecommendations();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return recommendations from Redis if exists")
    void getRecommendations_ShouldReturnFromRedis() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("recommendation:user:1")).thenReturn(List.of("prop-1"));
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(property));
        PropertyDetailedResponse mappedResponse = new PropertyDetailedResponse(
                "prop-1", "Villa", "ahmed", "", 100000.0, PropertyType.PURCHASE, PropertyStatus.AVAILABLE, null, null, null, null, null, null
                );
        when(propertyDetailedResponseMapper.apply(property)).thenReturn(mappedResponse);

        // When
        List<PropertyDetailedResponse> result = recommendationService.getRecommendations();

        // Then
        assertFalse(result.isEmpty());
        assertEquals("prop-1", result.get(0).id());
    }
}
