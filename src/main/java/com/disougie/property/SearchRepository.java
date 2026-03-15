package com.disougie.property;

import java.util.List;

import org.springframework.data.domain.Page;

import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyType;

public interface SearchRepository {
	
	Page<Property> findByText(String text, int page, int size);
	
	Page<Property> findByTextForAdmin(String text, int page, int size);
	
	Page<Property> findByFilters(PropertyType type,
			String city, String area, Integer minRooms, Integer maxRooms,Integer minBaths,
			Integer maxBaths, Double minPrice, Integer maxPrice, Integer minSize,
			Integer maxSize, int page, int size
	);
	
	List<Property> findNearByCoordinates(double lng, double lat, double maxDistance);

}
