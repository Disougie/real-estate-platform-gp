package com.disougie.property;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record PropertyPatchRequest(
		
		String title,
		String description,
		Double price,
		List<MultipartFile> images
		
) {
	
}
