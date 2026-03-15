package com.disougie.saved_property;

import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_EXCHANGE;
import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_ROUTING_KEY;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.property.PropertyRepository;
import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.entity.Property;
import com.disougie.recommendation.EventType;
import com.disougie.recommendation.RecommendationRequest;
import com.disougie.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavedPropertyService {

	private final SavedPropertyRepository savedPropertyRepository;
	private final SavedPropertyResponseMapper savedPropertyResponseMapper;
	private final PropertyRepository propertyRepository;
	private final RabbitTemplate rabbitTemplate;
	
	public List<PropertyDetailedResponse> getMySavedProperties() {
		AppUser currentUser = JwtService.getCurrentUser();
		return savedPropertyRepository
				.findByUser(currentUser)
				.stream()
				.map(savedPropertyResponseMapper)
				.toList();
	}

	public void saveProperty(SavePropertyRequest request) {
		
		AppUser currentUser = JwtService.getCurrentUser();
		
		Property property = propertyRepository
				.findById(request.property_id())
				.orElseThrow(() -> new ResourceNotFoundException("property not found"));
		
		SavedProperty savedProperty = new SavedProperty(
				new SavedPropertyId(
						currentUser.getId(), 
						request.property_id()
				),
				currentUser
		);
		savedPropertyRepository.save(savedProperty);
		
		rabbitTemplate.convertAndSend(
				RECOMMENDATION_EXCHANGE, 
				RECOMMENDATION_ROUTING_KEY, 
				new RecommendationRequest(
						currentUser.getId(),
						property.getLocation().getCity(),
						property.getLocation().getArea(),
						property.getType().toString(),
						property.getPrice(),
						property.getFeatures().getSize(),
						EventType.VIEW
				)
		);
	}

	public void removeFromSavedProperties(String id) {
		SavedPropertyId savedPropertyId = new SavedPropertyId(
				JwtService.getCurrentUser().getId(), id
		);
		savedPropertyRepository.deleteById(savedPropertyId);
	}

}
