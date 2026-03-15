package com.disougie.property;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.property.entity.Property;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PropertyAdminResponseMapper implements Function<Property, PropertyAdminResponse> {

	private final AppUserRepository appUserRepository;
	
	@Override
	public PropertyAdminResponse apply(Property property) {
		
		AppUser user = appUserRepository
				.findById(property.getOwnerId())
				.orElse(AppUser.builder().name("removed user").build());
		
		return new PropertyAdminResponse(
				property.getId(),
				property.getTitle(), 
				user.getName(), 
				property.getDescription(), 
				property.getPrice(), 
				property.getType(),
				property.getStatus(),
				property.getFeatures(), 
				property.getMapsLocation().getCoordinates(), 
				property.getLocation().getCity(), 
				property.getLocation().getArea(), 
				property.getReview()
		);
	}

}
