package com.disougie.app_user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	
	@Query(value = "select 1", nativeQuery = true)
	Integer keepAlive();
	
	@Query("from AppUser where email = :e")
	Optional<AppUser> findByEmail(@Param("e") String email);
	
	@Query(
		value =  "select * from app_user where email = :e", 
		nativeQuery = true
	)
	Optional<AppUser> findByEmailIncludingDeleted(@Param("e") String email);
	
	@Query(value = "select * from app_user where role = :role", nativeQuery = true)
	Page<AppUser> findByRoleForAdmin(Pageable pageable, @Param("role") String role);

	@Query(value = "SELECT * FROM app_user WHERE MATCH(name, email) AGAINST (:text IN NATURAL LANGUAGE MODE) AND role = :r",
			nativeQuery = true)
	Page<AppUser> searchByNameOrEmail(Pageable pageable, @Param("text") String text, @Param("r") String role);
	
}
