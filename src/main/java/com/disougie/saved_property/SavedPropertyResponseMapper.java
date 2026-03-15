package com.disougie.saved_property;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.property.PropertyRepository;
import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.PropertyDetailedResponseMapper;
import com.disougie.property.entity.Property;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavedPropertyResponseMapper implements Function<SavedProperty, PropertyDetailedResponse>{
	
	private final PropertyRepository propertyRepository;
	private final PropertyDetailedResponseMapper responseMapper;
	
	@Override
	public PropertyDetailedResponse apply(SavedProperty savedProperty) {
		return responseMapper.apply(
				propertyRepository
					.findById(savedProperty.getId().getPropertyId())
					.orElse(new Property())
		);
	}

}
