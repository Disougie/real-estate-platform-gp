package com.disougie.recommendation;

public record RecommendationRequest(
		Long userId,
		String propertyCity,
		String propertyArea,
		String propertyType,
		double propertyPrice,
		int propertySize,
		EventType eventType
) {

}
