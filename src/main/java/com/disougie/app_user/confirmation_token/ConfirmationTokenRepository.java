package com.disougie.app_user.confirmation_token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.disougie.app_user.AppUser;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
	
	@Query("from ConfirmationToken where token = :t")
	Optional<ConfirmationToken> findByToken(@Param("t") String token);
	
	@Query("from ConfirmationToken where user = :u")
	Optional<ConfirmationToken> findByAppUser(@Param("u") AppUser user);
}
