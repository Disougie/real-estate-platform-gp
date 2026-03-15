package com.disougie.saved_property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.disougie.app_user.AppUser;


public interface SavedPropertyRepository extends JpaRepository<SavedProperty, SavedPropertyId>{

	@Query("from SavedProperty where user = :current_user")
	List<SavedProperty> findByUser(@Param("current_user") AppUser user);
	
	
}
