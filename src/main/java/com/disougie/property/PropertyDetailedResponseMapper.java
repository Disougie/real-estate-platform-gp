package com.disougie.property;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.property.entity.Property;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PropertyDetailedResponseMapper implements Function<Property, PropertyDetailedResponse> {

	private final AppUserRepository appUserRepository;
	
	@Override
	public PropertyDetailedResponse apply(Property property) {
		
		AppUser owner = appUserRepository.findById(property.getOwnerId()).get();
		
		List<String> imagesUrls = null;
		if(property.getImages() != null) {
			imagesUrls = property
					.getImages()
					.stream()
					.map(image -> image.getImageUrl())
					.toList();			
		}
				
		return new PropertyDetailedResponse(
				property.getId(),
				property.getTitle(), 
				owner.getName(), 
				property.getDescription(), 
				property.getPrice(), 
				property.getType(),
				property.getStatus(),
				property.getFeatures(), 
				property.getMapsLocation().getCoordinates(), 
				property.getLocation().getCity(), 
				property.getLocation().getArea(), 
				imagesUrls,
				property.getReview()
		);
	}

	
	
}
