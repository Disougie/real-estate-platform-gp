package com.disougie.saved_property;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class SavedPropertyId implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long userId;
	private String propertyId;

}
