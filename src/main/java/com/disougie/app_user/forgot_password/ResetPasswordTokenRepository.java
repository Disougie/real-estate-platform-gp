package com.disougie.app_user.forgot_password;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
	
	@Query("from ResetPasswordToken where token = :token")
	Optional<ResetPasswordToken> findByToken(@Param("token") String token);
	
}
