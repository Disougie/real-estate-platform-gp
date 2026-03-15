package com.disougie.property;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.property.entity.Image;
import com.disougie.property.entity.Property;

@Component
public class PropertyBriefResponseMapper implements Function<Property, PropertyBriefResponse> {

	@Override
	public PropertyBriefResponse apply(Property property) {
		
		List<Image> images = property.getImages();
		String imageUrl = null;
		
		if(images != null && !images.isEmpty())
			imageUrl = images.getFirst().getImageUrl();
		
		return new PropertyBriefResponse(
				property.getId(), 
				property.getTitle(), 
				property.getPrice(), 
				property.getLocation().getArea(), 
				imageUrl
		);
	}

}
