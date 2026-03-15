package com.disougie.intial_contract;

import java.time.LocalDateTime;

import com.disougie.app_user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class InitialContract {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;
	
	@ManyToOne
	@JoinColumn(name = "seeker_id", nullable = false)
	private AppUser seeker;
	
	@ManyToOne
	@JoinColumn(name = "lawyer_id")
	private AppUser lawyer;
	
	@Column(nullable = false)
	private String property_id;
	
	@Column(nullable = false)
	private InitialContractType type;
	
	@Column(nullable = false)
	private InitialContractStatus status;
	
	@Column(nullable = false)
	private LocalDateTime created_at;
	
	@Column(nullable = false)
	private LocalDateTime expire_at;
	
	private Integer rentDuration;
	
	@Column(nullable = false)
	private Double overall_contract_amount;
	
}
