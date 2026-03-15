package com.disougie.property;

import java.util.List;

import com.disougie.property.entity.Features;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.property.entity.Review;

public record PropertyAdminResponse(
		String id,
		String title,
		String owner,
		String decription,
		double price,
		PropertyType type,
		PropertyStatus status,
		Features features, 
		List<Double> coordinates,
		String city,
		String area,
		Review review
) {

}
