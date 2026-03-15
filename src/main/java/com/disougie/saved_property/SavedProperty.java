package com.disougie.saved_property;

import com.disougie.app_user.AppUser;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SavedProperty {
	
	@EmbeddedId
	private SavedPropertyId id;
	
	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name ="user_id")
	private AppUser user;

}
