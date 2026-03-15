package com.disougie.app_user.lawyer;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disougie.app_user.AppUser;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.intial_contract.InitialContract;
import com.disougie.intial_contract.InitialContractRepository;
import com.disougie.intial_contract.InitialContractResponseMapper;
import com.disougie.intial_contract.InitialContractStatus;
import com.disougie.intial_contract.initialContractResponse;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
	
	private final InitialContractRepository initialContractRepository;
	private final InitialContractResponseMapper initialContractResponseMapper;
	private final PropertyRepository propertyRepository;

	public List<initialContractResponse> getPendingContracts() {
		return initialContractRepository
				.findByStatus(InitialContractStatus.PENDING_PROCESSING)
				.stream()
				.map(initialContractResponseMapper)
				.toList();
	}

	public initialContractResponse getContract(long id) {
		return initialContractRepository.findById(id)
				.map(initialContractResponseMapper)
				.orElseThrow(() -> new ResourceNotFoundException("contract not found"));
	}

	public List<initialContractResponse> getMyContracts() {
		return initialContractRepository
				.findByLawyer(JwtService.getCurrentUser())
				.stream()
				.map(initialContractResponseMapper)
				.toList();
	}
	
	public void workingOnContract(long id) {
		
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		
		AppUser lawyer = JwtService.getCurrentUser();
		
		contract.setLawyer(lawyer);
		
		contract.setStatus(InitialContractStatus.UNDER_PROCESS);
		
		initialContractRepository.save(contract);
		
	}

	@Transactional
	public void completeContract(long id) {

		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		
		Property property = propertyRepository.findById(contract.getProperty_id()).orElseThrow(
				() -> new ResourceNotFoundException("property not found")
		);
		
		if(contract.getLawyer() == null || contract.getLawyer().getId() != JwtService.getCurrentUser().getId()) {
			throw new AccessDeniedException("Not authorize to do this action");
		}
		
		
		
		contract.setStatus(InitialContractStatus.COMPLETED);
		
		property.setStatus(PropertyStatus.COMPLETED);
		
		initialContractRepository.save(contract);
		
		propertyRepository.save(property);
		
	}

	public void cancelContract(long id) {
		
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		
		if(contract.getLawyer() == null || contract.getLawyer().getId() != JwtService.getCurrentUser().getId()) {
			throw new AccessDeniedException("Not Auhtorize to proccess this contract");
		}
		
		contract.setLawyer(null);
		contract.setStatus(InitialContractStatus.PENDING_PROCESSING);
		
		initialContractRepository.save(contract);
		
	}

	public void banContract(long id) {
		
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		
		if(contract.getLawyer() == null || contract.getLawyer().getId() != JwtService.getCurrentUser().getId()) {
			throw new AccessDeniedException("Not Auhtorize to proccess this contract");
		}
		
		Property property = propertyRepository.findById(contract.getProperty_id()).orElseThrow(
				() -> new ResourceNotFoundException("property not found")
		);
		
		contract.setStatus(InitialContractStatus.BANNED);
		
		property.setStatus(PropertyStatus.BANNED);
		
		initialContractRepository.save(contract);
		
		propertyRepository.save(property);
		
	}

}
