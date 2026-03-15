package com.disougie.recommendation;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {

	@Query("{userId: ?0}")
	Optional<UserPreference> findByUserId(Long userId);
	
}
