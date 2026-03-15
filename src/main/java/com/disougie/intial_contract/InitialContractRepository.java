package com.disougie.intial_contract;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.disougie.app_user.AppUser;


@Repository
public interface InitialContractRepository extends JpaRepository<InitialContract, Long>{

	@Query("from InitialContract where owner = :owner")
	List<InitialContract> findByOwner(@Param("owner") AppUser owner);
	
	@Query("from InitialContract where owner = :user or seeker = :user")
	List<InitialContract> findByUser(@Param("user") AppUser user);
	
	@Query("from InitialContract where property_id = :id")
	List<InitialContract> findByPropertyId(@Param("id") String property_id);
	
	@Query("from InitialContract where status = :status")
	List<InitialContract> findByStatus(@Param("status") InitialContractStatus status);
	
	@Query("from InitialContract where lawyer = :lawyer")
	List<InitialContract> findByLawyer(@Param("lawyer")AppUser lawyer);
	
}
