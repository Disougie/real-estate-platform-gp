package com.disougie.saved_property;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.property.PropertyDetailedResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/saved-properties")
@RequiredArgsConstructor
public class SevedPropertyController {
	
	private final SavedPropertyService savedPropertyService;
	
	@GetMapping
	public ResponseEntity<List<PropertyDetailedResponse>> getMySavedProperties(){
		List<PropertyDetailedResponse> response = savedPropertyService.getMySavedProperties();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping
	public ResponseEntity<?> saveProperty(@Validated @RequestBody SavePropertyRequest request){
		savedPropertyService.saveProperty(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity<?> removeFromSaved(@PathVariable String id){
		savedPropertyService.removeFromSavedProperties(id);
		return ResponseEntity.noContent().build();
	}
	
}
