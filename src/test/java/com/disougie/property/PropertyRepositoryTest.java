package com.disougie.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.disougie.property.entity.Features;
import com.disougie.property.entity.Location;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;

@DataMongoTest
public class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    private Property availableProperty;
    private Property pendingProperty;

    @BeforeEach
    void setUp() {
        availableProperty = createProperty(
                "Available Villa",
                150000.0,
                PropertyType.PURCHASE,
                1L,
                PropertyStatus.AVAILABLE,
                "Cairo",
                "Maadi",
                3,
                200
        );
        pendingProperty = createProperty(
                "Pending Apartment",
                5000.0,
                PropertyType.RENT,
                2L,
                PropertyStatus.PENDING_PROCESSING,
                "Giza",
                "Dokki",
                2,
                120
        );

        propertyRepository.saveAll(List.of(availableProperty, pendingProperty));
    }

    @AfterEach
    void tearDown() {
        propertyRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return only AVAILABLE properties when findAllApprovedProperties is called")
    void shouldReturnOnlyAvailableProperties() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Property> result = propertyRepository.findAllApprovedProperties(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(PropertyStatus.AVAILABLE, result.getContent().get(0).getStatus());
        assertEquals("Available Villa", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Should return properties belonging to a specific owner ID")
    void shouldReturnPropertiesByOwnerId() {
        // Given
        Long ownerId = 1L;

        // When
        List<Property> result = propertyRepository.findByOwnerId(ownerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
    }

    @Test
    @DisplayName("Should return empty list when searching by non-existent owner ID")
    void shouldReturnEmptyListWhenOwnerIdDoesNotExist() {
        // Given
        Long nonExistentOwnerId = 99L;

        // When
        List<Property> result = propertyRepository.findByOwnerId(nonExistentOwnerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find properties matching user preferences (city, area, type, price, size)")
    void shouldFindPropertiesByUserPreferences() {
        // Given
        Set<String> cities = Set.of("Cairo");
        Set<String> areas = Set.of("Maadi");
        Set<String> types = Set.of(PropertyType.PURCHASE.name());
        double minPrice = 100000.0;
        double maxPrice = 200000.0;
        int minSize = 100;
        int maxSize = 300;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<Property> result = propertyRepository.findUserPrefrencePropertyies(
                cities, areas, types, minPrice, maxPrice, minSize, maxSize, pageable);

        // Then
        assertNotNull(result);
        assertThat(result.size()).isGreaterThanOrEqualTo(1);
        boolean found = result.stream().anyMatch(p -> p.getTitle().equals("Available Villa"));
        assertTrue(found, "The available villa should match the preferences");
    }

    // Helper method to create Property entity
    private Property createProperty(String title, double price, PropertyType type, Long ownerId, 
                                    PropertyStatus status, String city, String area, 
                                    int rooms, int size) {
        Location location = new Location();
        location.setCity(city);
        location.setArea(area);

        Features features = new Features();
        features.setRooms(rooms);
        features.setSize(size);

        return Property.builder()
                .title(title)
                .price(price)
                .type(type)
                .ownerId(ownerId)
                .status(status)
                .location(location)
                .features(features)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
