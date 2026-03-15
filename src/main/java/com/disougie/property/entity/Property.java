package com.disougie.property.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "property")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Property {
	
	@Id
	private String id;
	
	private String title;
	
	private double price;
	
	@Enumerated(EnumType.STRING)
	private PropertyType type;
	
	private Long ownerId;
	
	@Enumerated(EnumType.STRING)
	private PropertyStatus status;
	
	@Embedded
	private Features features;
	
//	@GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
	private GeoJsonPoint mapsLocation;
	
	@Embedded
	private Location location;
	
	@Embedded
	private List<Image> images;
	
	private String description;
	
	@Embedded
	private Review review;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime deletedAt;
		
}
