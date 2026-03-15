package com.disougie.property;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.disougie.property.entity.PropertyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PropertyAdPostRequest(
		
		@NotBlank
		String title,
		@NotNull
		Double price,
		@NotNull
		PropertyType type,
		@NotNull
		Double lng,
		@NotNull
		Double lat,
		String description,
		Integer rooms,
		Integer baths,
		List<MultipartFile> images,
		@NotBlank
		String city,
		@NotBlank
		String area,
		@NotNull
		Integer size
) {

}
