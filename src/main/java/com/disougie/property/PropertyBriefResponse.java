package com.disougie.property;

public record PropertyBriefResponse(
		String id,
		String title,
		double price,
		String area,
		String imageUrl
) {

}
