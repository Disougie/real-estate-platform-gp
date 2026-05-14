package com.disougie.property;

import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_EXCHANGE;
import static com.disougie.recommendation.RecommendationMQConfig.RECOMMENDATION_ROUTING_KEY;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.imagekit.ImageService;
import com.disougie.property.entity.Features;
import com.disougie.property.entity.Image;
import com.disougie.property.entity.Location;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.property.entity.Review;
import com.disougie.recommendation.EventType;
import com.disougie.recommendation.RecommendationRequest;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {
	private final PropertyDetailedResponseMapper propertyDetailedResponseMapper;
	private final PropertyBriefResponseMapper propertyBriefResponseMapper;
	@SuppressWarnings("rawtypes")
	private final PageResponseMapper pageResponseMapper;
	private final PropertyRepository propertyRepository;
	private final ImageService imageService;
	private final RabbitTemplate rabbitTemplate;
	
	@SuppressWarnings("unchecked")
	public PageResponse<PropertyBriefResponse> getProperties(int page, int size) {
		
		Page<PropertyBriefResponse> pageOfProperties = propertyRepository
				.findAllApprovedProperties(PageRequest.of(page, size))
				.map(propertyBriefResponseMapper);
		
		return pageResponseMapper.apply(pageOfProperties);
		
	}
	
	@Cacheable(key = "#id", value = "properties")
	public PropertyDetailedResponse getProperty(String id) {
		
		AppUser user = JwtService.getCurrentUser();
		Property property = propertyRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("property not found"));
		
		rabbitTemplate.convertAndSend(
				RECOMMENDATION_EXCHANGE, 
				RECOMMENDATION_ROUTING_KEY, 
				new RecommendationRequest(
						user.getId(),
						property.getLocation().getCity(),
						property.getLocation().getArea(),
						property.getType().toString(),
						property.getPrice(),
						property.getFeatures().getSize(),
						EventType.VIEW
				)
		);
		
		return propertyDetailedResponseMapper.apply(property);
	}
	
	public PropertyDetailedResponse getMyProperty(String id) {
		
		AppUser owner = JwtService.getCurrentUser();
		Property property = propertyRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("property not found"));
		
		if(property.getOwnerId() != owner.getId()) {
			throw new AccessDeniedException("Not Authorize to view this property");
		}
		
		return propertyDetailedResponseMapper.apply(property);
	}
	
	
	public List<Property> getMyProperties() {
		AppUser owner = JwtService.getCurrentUser();
		return propertyRepository.findByOwnerId(owner.getId());
	}
	
	public PropertyAdCreationResponse addPropertyAd(PropertyAdPostRequest request) {
		
		AppUser owner = JwtService.getCurrentUser();
		
		List<Image> images = null;
		
		if(request.images() != null)
			images  = imageService.uploadImages(request.images());
		
		try {
			Property property = Property.builder()
					.ownerId(owner.getId())
					.title(request.title())
					.description(request.description())
					.price(request.price())
					.type(request.type())
					.mapsLocation(new GeoJsonPoint(request.lng(), request.lat()))
					.location(
							new Location(
								request.city(), 
								request.area() 
							)
					)
					.features(
							new Features(
								request.rooms(), 
								request.baths(), 
								request.size()
							)
					)
					.images(images)
					.status(PropertyStatus.AVAILABLE)
					.build();
			
			property = propertyRepository.save(property);
			
			return new PropertyAdCreationResponse(
					property.getId()
			);
		}
		catch (Exception e) {
			if(images != null && !images.isEmpty()) {
				for(Image image : images)			
					imageService.deleteImage(image.getFileId());				
			}
			throw e;
		}
		
	}
	
	@CacheEvict(key = "#id", value = "properties")
	public void deletePropertyAd(String id) {
		
		Property property = propertyRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Property not found")
		);
		
		AppUser owner = JwtService.getCurrentUser();
		
		if(property.getOwnerId() != owner.getId()) {
			throw new AccessDeniedException(
					"your are not authorized to access this property"
			);
		}
		
		propertyRepository.delete(property);
		
	}

	@CacheEvict(key = "#id", value = "properties")
	public void changePropertyAd(String id, PropertyPatchRequest request) {
		
		Property property = propertyRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("Property not found")
		);
		
		AppUser owner = JwtService.getCurrentUser();
		
		if(property.getOwnerId() != owner.getId()) {
			throw new AccessDeniedException(
					"your are not authorized to access this property"
			);
		}
		
		if(request.title() != null)
			property.setTitle(request.title());
		
		if(request.description() != null)
			property.setDescription(request.description());
		
		if(request.price() != null)
			property.setPrice(request.price());
		
		if(request.images() != null) {
			List<String> filesId = property
					.getImages()
					.stream()
					.map(image -> image.getFileId())
					.toList();
			
			for(String fileId: filesId)
				imageService.deleteImage(fileId);
			
			List<Image> newImages = imageService.uploadImages(request.images());
			property.setImages(newImages);
		}
		
		propertyRepository.save(property);
		
	}

	@SuppressWarnings("unchecked")
	public PageResponse<PropertyBriefResponse> searchByText(String text, int page, int size) {
		
		Page<PropertyBriefResponse> response = propertyRepository
				.findByText(text, page, size)
				.map(propertyBriefResponseMapper);
		
		return pageResponseMapper.apply(response);
	}

	@SuppressWarnings("unchecked")
	public PageResponse<PropertyBriefResponse> searchByFilters(PropertyType type,
			String city, String area, Integer minRooms, Integer maxRooms,Integer minBaths,
			Integer maxBaths, Double minPrice, Integer maxPrice, Integer minSize,
			Integer maxSize, int page, int size
	) {
		Page<PropertyBriefResponse> response = propertyRepository
				.findByFilters(
					type, city, area, minRooms, maxRooms, 
					minBaths, maxBaths, minPrice, maxPrice, 
					minSize, maxSize, page, size
				)
				.map(propertyBriefResponseMapper);
		
		return pageResponseMapper.apply(response);
	}

	public List<PropertyBriefResponse> searchByCoordinates(double lng, double lat, Double maxDistance) {
		if(maxDistance == null)
			maxDistance = 1.0;
		return propertyRepository.findNearByCoordinates(lng, lat, maxDistance)
			.stream()
			.map(propertyBriefResponseMapper)
			.toList();
	}

	public void ratePropertyAd(String id, ReviewRequest request) {
		
		Property property = propertyRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("property not found")
		);
		
		if(!property.getStatus().equals(PropertyStatus.AVAILABLE)) {
			throw new AccessDeniedException("not allowed to review this property");
		}
		
		if(property.getReview() == null)
			property.setReview(new Review(request.stars()));
		else
			property.getReview().mergeReviews(new Review(request.stars()));
		
		propertyRepository.save(property);
		
	}

}
