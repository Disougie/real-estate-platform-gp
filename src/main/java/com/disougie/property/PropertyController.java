package com.disougie.property;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyType;
import com.disougie.util.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
	
	private final PropertyService propertyService;
	
	@GetMapping
	public ResponseEntity<PageResponse<PropertyBriefResponse>> getProperties(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "20") int size){
		PageResponse<PropertyBriefResponse> properties = propertyService.getProperties(page,size);
		return ResponseEntity.status(HttpStatus.OK).body(properties);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<PropertyDetailedResponse> getProperty(@PathVariable String id){
		PropertyDetailedResponse property = propertyService.getProperty(id);
		return ResponseEntity.status(HttpStatus.OK).body(property);
	}
	
	@GetMapping("/my-property/{id}")
	public ResponseEntity<PropertyDetailedResponse> getMyProperty(@PathVariable String id){
		PropertyDetailedResponse property = propertyService.getMyProperty(id);
		return ResponseEntity.status(HttpStatus.OK).body(property);
	}
	
	@GetMapping("/my-properties")
	public ResponseEntity<List<Property>> getMyProperties(){
		List<Property> myProperties = propertyService.getMyProperties();
		return ResponseEntity.status(HttpStatus.OK).body(myProperties);
	}
	
	@PostMapping
	public ResponseEntity<PropertyAdCreationResponse> addPropertyAd(
								@Validated @RequestBody PropertyAdPostRequest request){
		PropertyAdCreationResponse response = propertyService.addPropertyAd(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity<?> deletePropertyAd(@PathVariable String id){
		propertyService.deletePropertyAd(id);
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("{id}")
	public ResponseEntity<?> changePropertyAd(@PathVariable String id, 
											  @RequestBody PropertyPatchRequest request){
		propertyService.changePropertyAd(id,request);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("search")
	public ResponseEntity<PageResponse<PropertyBriefResponse>> searchByText(
									@RequestParam String text,								
									@RequestParam(defaultValue = "0") int page,
									@RequestParam(defaultValue = "20") int size){
		PageResponse<PropertyBriefResponse> properties = propertyService
				.searchByText(text, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(properties);
	}
	
	@GetMapping("search/filter")
	public ResponseEntity<PageResponse<PropertyBriefResponse>> searchByFilters(
										@RequestParam(required = false) PropertyType type,
										@RequestParam(required = false) String city,
										@RequestParam(required = false) String area,
										@RequestParam(required = false) Integer minRooms,
										@RequestParam(required = false) Integer maxRooms,
										@RequestParam(required = false) Integer minBaths,
										@RequestParam(required = false) Integer maxBaths,
										@RequestParam(required = false) Double minPrice,
										@RequestParam(required = false) Integer maxPrice,
										@RequestParam(required = false) Integer minSize,
										@RequestParam(required = false) Integer maxSize,
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "20") int size){
		PageResponse<PropertyBriefResponse> properties = propertyService
				.searchByFilters(
						type, city, area, minRooms, maxRooms, 
						minBaths, maxBaths, minPrice, maxPrice, 
						minSize, maxSize, page, size
				);
		
		return ResponseEntity.status(HttpStatus.OK).body(properties);
	}
	
	@GetMapping("search/coord")
	public ResponseEntity<List<PropertyBriefResponse>> searchByCoordinates(
										@RequestParam double lng, 
										@RequestParam double lat,
										@RequestParam(required = false) Double maxDistance){
		List<PropertyBriefResponse> response = propertyService.searchByCoordinates(lng,lat,maxDistance);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping("rating/{id}")
	public ResponseEntity<?> ratePropertyAd(
								@PathVariable String id, 
								@Validated @RequestBody ReviewRequest reviewRequest){
		propertyService.ratePropertyAd(id, reviewRequest);
		return ResponseEntity.noContent().build();
	}

}
