package com.disougie.property;

import java.util.List;

import com.disougie.property.entity.Features;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.property.entity.Review;

public record PropertyDetailedResponse(
		
		String id,
		String title,
		String ownerName,
		String decription,
		double price,
		PropertyType type,
		PropertyStatus status,
		Features features, 
		List<Double> coordinates,
		String city,
		String area,
		List<String> imagesUrls,
		Review review
		
) {

}
