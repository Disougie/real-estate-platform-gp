package com.disougie.property;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.property.entity.Property;

@Component
public class PropertyMapResponseMapper implements Function<Property, PropertyMapResponse> {

	@Override
	public PropertyMapResponse apply(Property property) {
		String imageUrl = null;
		if(property.getImages() != null && !property.getImages().isEmpty()) {
			imageUrl = property.getImages().getFirst().getImageUrl();
		}
		return new PropertyMapResponse(
				property.getId(), 
				property.getTitle(), 
				property.getLocation().getArea(), 
				property.getType(), 
				property.getPrice(), 
				property.getMapsLocation().getCoordinates(),
				imageUrl
		);
	}

}
