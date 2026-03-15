package com.disougie.recommendation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.property.PropertyRepository;
import com.disougie.property.PropertyDetailedResponse;
import com.disougie.property.PropertyDetailedResponseMapper;
import com.disougie.property.entity.Property;
import com.disougie.security.JwtService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
	
	private final UserPreferenceRepository userPreferenceRepository;
	private final PropertyRepository propertyRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final PropertyDetailedResponseMapper propertyDetailedResponseMapper;
	
	@RabbitListener(queues = "#{recommmendationQueue.name}", ackMode = "MANUAL")
	public void updateUserPreferences(
			RecommendationRequest request,
			Message message, 
			Channel channel
	) throws IOException {
		
		MessageProperties messageProperties = message.getMessageProperties();
		try {	
			int requestValue = request.eventType().equals(EventType.FAVOURITE) ? 3 : 1;
			
			Optional<UserPreference> userPreferenceOptional = userPreferenceRepository
					.findByUserId(request.userId());
			UserPreference userPreference;
	
			String propertyCity = request.propertyCity();
			String propertyArea = request.propertyArea();
			String propertyType = request.propertyType();
			double propertyPrice = request.propertyPrice();
			int propertySize = request.propertySize();
			
			if(!userPreferenceOptional.isPresent()) {
				userPreference = UserPreference.builder()
						.userId(request.userId())
						.preferencedCity(Map.of(propertyCity, requestValue))
						.preferencedArea(Map.of(propertyArea, requestValue))
						.preferencedType(Map.of(propertyType, requestValue))
						.preferencedPrice(propertyPrice)
						.preferencedSize(propertySize)
						.preferencedCont(1)
						.build();
				
				userPreferenceRepository.save(userPreference);
			}
			else {
				userPreference = userPreferenceOptional.get();
				userPreference.getPreferencedCity().put(
						propertyCity,
						userPreference.getPreferencedCity().getOrDefault(propertyCity, 0) + requestValue
				);
				userPreference.getPreferencedArea().put(
						propertyArea,
						userPreference.getPreferencedCity().getOrDefault(propertyArea, 0) + requestValue
				);
				userPreference.getPreferencedType().put(
						propertyType,
						userPreference.getPreferencedCity().getOrDefault(propertyType, 0) + requestValue
				);
				userPreference.setPreferencedCont(
						userPreference.getPreferencedCont() + 1
				);
				userPreference.setPreferencedPrice(
						(userPreference.getPreferencedPrice() + propertyPrice) / userPreference.getPreferencedCont()
				);
				userPreference.setPreferencedSize(
						(userPreference.getPreferencedSize() + propertySize) / userPreference.getPreferencedCont()
				);
				
				userPreferenceRepository.save(userPreference);
			}
			
			if(userPreference.getPreferencedCont() % 5 == 0) {
				computRecommendations(userPreference);
			}
			
			channel.basicAck(messageProperties.getDeliveryTag(), false);
		}
		catch (Exception e) {
			channel.basicAck(messageProperties.getDeliveryTag(), false);
		}
	}
	
	public void computRecommendations(UserPreference preference) {
		
		double preferencedPrice = preference.getPreferencedPrice();
		int preferencedSize = preference.getPreferencedSize();
		List<Property> properties = propertyRepository.findUserPrefrencePropertyies(
				preference.getPreferencedCity().keySet(), 
				preference.getPreferencedArea().keySet(), 
				preference.getPreferencedType().keySet(), 
				preferencedPrice - preferencedPrice * 0.2, 
				preferencedPrice + preferencedPrice * 0.2, 
				(int)(preferencedSize - preferencedSize * 0.2),
				(int)(preferencedSize + preferencedSize * 0.2),
				PageRequest.of(0, 20)
		);
		
		int maxAreaWeight = 0;
		for(int value : preference.getPreferencedArea().values()) 
			maxAreaWeight = Math.max(maxAreaWeight, value);
		
		int maxCityWeight = 0;
		for(int value : preference.getPreferencedCity().values()) 
			maxCityWeight = Math.max(maxCityWeight, value);
		
		int maxTypeWeight = 0;
		for(int value : preference.getPreferencedType().values()) 
			maxTypeWeight = Math.max(maxTypeWeight, value);
		
		
		PriorityQueue<RecommendationItem> recommendations = 
				new PriorityQueue<>((r1,r2) -> (int)(r1.score()*100 - r2.score()*100));
		
		/*
		 * Cost:
		 *   city = 0.3
		 *   price = 0.3
		 *   type = 0.2
		 *   area = 0.1
		 *   size = 0.1
		 */
		
		for(Property property : properties) {
			double cityScore = preference.getPreferencedCity().getOrDefault(property.getLocation().getCity(), 0) / maxCityWeight;
			double areaScore = preference.getPreferencedArea().getOrDefault(property.getLocation().getArea(), 0) / maxAreaWeight;
			double typeScore = preference.getPreferencedType().getOrDefault(property.getType(), 0) / maxTypeWeight;
			double priceScore = 1 - (property.getPrice() - preferencedPrice) / preferencedPrice;
			double sizeScore = 1 - (property.getFeatures().getSize() - preferencedSize) / preferencedSize;
			double score = cityScore * 0.3 + 
						   priceScore * 0.3 + 
						   typeScore * 0.2 + 
						   areaScore * 0.1 + 
						   sizeScore * 0.1;
			recommendations.offer(new RecommendationItem(property.getId(), score));
			if(recommendations.size() > 10)
				recommendations.poll();
		}
		
		List<String> result = new ArrayList<>(
				recommendations.stream().map(r -> r.id()).toList()
		);
		
		redisTemplate.opsForValue().set(
				"recommendation:user:" + preference.getUserId(),
				result,
				15,
				TimeUnit.MINUTES
		);
	}


	@SuppressWarnings("unchecked")
	public List<PropertyDetailedResponse> getRecommendations() {
		AppUser user = JwtService.getCurrentUser(); 
		List<String> propertyIds = (List<String>)redisTemplate
				.opsForValue()
				.get("recommendation:user:" + user.getId());
		
		if(propertyIds == null) {
			Optional<UserPreference> userPreferenceOptional = userPreferenceRepository
					.findByUserId(user.getId());
			
			if(!userPreferenceOptional.isPresent())
				return new ArrayList<>();			
			
			computRecommendations(userPreferenceOptional.get());
			
			
			propertyIds = (List<String>)redisTemplate
					.opsForValue()
					.get("recommendation:user:" + user.getId());
		}
		
		List<PropertyDetailedResponse> recommmendations =  new ArrayList<>();
		
		for(String id : propertyIds) {
			recommmendations.add(
					propertyRepository
							.findById(id)
							.map(propertyDetailedResponseMapper)
							.get()
			);
		}
		return recommmendations;
	}
}
