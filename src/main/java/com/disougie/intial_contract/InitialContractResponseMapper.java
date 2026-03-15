package com.disougie.intial_contract;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.disougie.exception.ResourceNotFoundException;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitialContractResponseMapper implements Function<InitialContract, initialContractResponse>{
	
	private final PropertyRepository propertyRepository;
	
	@Override
	public initialContractResponse apply(InitialContract initialContract) {
		
		Property property = propertyRepository
				.findById(initialContract.getProperty_id())
				.orElseThrow(() -> new ResourceNotFoundException("property not found"));
		
		return new initialContractResponse(
				initialContract.getId(),
				new ContractData(
						initialContract.getId(), 
						"Real Estate Platform", 
						initialContract.getCreated_at().toString(), 
						initialContract.getExpire_at().toString()
				), 
				new UserData(
						initialContract.getOwner().getName(), 
						initialContract.getOwner().getPhone(), 
						initialContract.getOwner().getEmail()
				), 
				new UserData(
						initialContract.getSeeker().getName(), 
						initialContract.getSeeker().getPhone(), 
						initialContract.getSeeker().getEmail()
				), 
				new PropertyData(
						initialContract.getType().toString(), 
						property.getFeatures().getSize() + "", 
						property.getFeatures().getRooms()+ ""
				), 
				new FinancialData(
						initialContract.getRentDuration(),
						initialContract.getOverall_contract_amount()
				),
				initialContract.getStatus()
		);
	}
	
}
