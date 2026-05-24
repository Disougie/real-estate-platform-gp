package com.disougie.property;

import java.util.List;

import com.disougie.property.entity.PropertyType;

public record PropertyMapResponse(
		String id,
		String title,
		String location,
		PropertyType type,
		double price,
		List<Double> position,
		String imageUrl
) {

}
