package com.disougie.recommendation;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "user_preference")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class UserPreference {
	
	@Id
	private String id;
	private Long userId;
	private Map<String,Integer> preferencedCity;
	private Map<String,Integer> preferencedArea;
	private Map<String,Integer> preferencedType;
	private double preferencedPrice;
	private int preferencedSize;
	private int preferencedCont;

}
