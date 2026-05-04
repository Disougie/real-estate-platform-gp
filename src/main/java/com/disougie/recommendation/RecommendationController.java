package com.disougie.recommendation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.property.PropertyDetailedResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/recommendation")
@RequiredArgsConstructor
@SecurityRequirement(
		name = "BearerAuth"
)
public class RecommendationController {
	
	private final RecommendationService recommendationService;
	
	@GetMapping
	public ResponseEntity<List<PropertyDetailedResponse>> getRecommendations(){
		List<PropertyDetailedResponse> properties = recommendationService.getRecommendations();
		return ResponseEntity.ok(properties);
	}
	
}
