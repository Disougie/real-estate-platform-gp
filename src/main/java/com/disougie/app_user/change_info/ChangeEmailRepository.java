package com.disougie.app_user.change_info;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.confirmation_token.ConfirmationToken;



@Repository
public interface ChangeEmailRepository extends JpaRepository<ChangeEmail, Long>{
	
	@Query("from ChangeEmail where user = :user")
	Optional<ChangeEmail> findByUser(@Param("user") AppUser user);
	
	@Query("from ChangeEmail where confirmationToken = :token")
	Optional<ChangeEmail> findByConfirmationToken(@Param("token") ConfirmationToken confirmationToken);
	
}
