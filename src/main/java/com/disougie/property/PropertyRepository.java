package com.disougie.property;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.disougie.property.entity.Property;

@Repository
public interface PropertyRepository extends MongoRepository<Property, String>, SearchRepository {
	
	@Query("{status: AVAILABLE}")
	Page<Property> findAllApprovedProperties(Pageable pageable);
	
	@Query("{ownerId : ?0}")
	List<Property> findByOwnerId(Long ownerId);
	
	@Query("{$or: [{\"location.city\":{$in: ?0}}, {\"location.area\": {$in: ?1}}, {type: {$in: ?2}}, {price: {$lte: ?3, $gte: ?4}}, {\"features.size\": {$lte: ?5, $gte: ?6}}]}")
	List<Property> findUserPrefrencePropertyies(Collection<String> city, Collection<String> area, Collection<String> type, double minprice, double maxPrice, int minSize, int maxSize, Pageable pageable);
	

}
