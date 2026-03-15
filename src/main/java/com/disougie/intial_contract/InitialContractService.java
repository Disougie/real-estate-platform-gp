package com.disougie.intial_contract;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.notification.NotificationService;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.security.JwtService;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitialContractService {
	
	private final InitialContractRepository initialContractRepository;
	private final InitialContractResponseMapper initialContractResponseMapper;
	private final PropertyRepository propertyRepository;
	private final AppUserRepository appUserRepository;
	private final NotificationService notificationService;


	public List<initialContractResponse> getMyInitialContracts() {
		
		AppUser user = JwtService.getCurrentUser();
		
		return initialContractRepository
				.findByUser(user)
				.stream()
				.map(initialContractResponseMapper)
				.toList();
	}
	
	
	public initialContractResponse getInitialContract(Long id) {
		
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		
		long currentUserId = JwtService.getCurrentUser().getId();
		
		if(currentUserId != contract.getOwner().getId()) {
			throw new AccessDeniedException("you are not authorized to process this contract");
		}
		
		return initialContractResponseMapper.apply(contract);
	}

	
	@Transactional
	public InitialContractCreationResponse createInitialContract(InitialContractCreationRequest request) {
		
		Property property = propertyRepository.findById(request.property_id()).orElseThrow(
				() -> new ResourceNotFoundException("property not found")
		);
		
		
		AppUser owner = appUserRepository.findById(property.getOwnerId()).orElseThrow(
				() -> new ResourceNotFoundException("owner not found")
		);
		
		AppUser seeker = JwtService.getCurrentUser();
		
		PropertyType propertyType = property.getType();
		InitialContractType contractType = null;
		Integer rentDuration = null;
		double overallContractAmount = 0;
		
		switch(propertyType) {
			case RENT:
			case COMMERCIAL_RENT:
			case STUDENT:
				contractType = InitialContractType.RENT; 
				if(request.rentDuration() != null) {
					rentDuration = request.rentDuration();
					overallContractAmount = rentDuration * property.getPrice();
				}
				else
					overallContractAmount = property.getPrice();
				break;
			case PURCHASE:
			case COMMERCIAL_PURCHASE:
				contractType = InitialContractType.PURCHASE;
				overallContractAmount = property.getPrice();
		};
		
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Africa/Khartoum"));
		
		InitialContract initialContract = InitialContract.builder()
				.property_id(request.property_id())
				.owner(owner)
				.seeker(seeker)
				.type(contractType)
				.status(InitialContractStatus.PENDING_APPROVAL)
				.created_at(now)
				.expire_at(now.plusMinutes(15))
				.rentDuration(rentDuration)
				.overall_contract_amount(overallContractAmount)
				.build();
		
		initialContract = initialContractRepository.save(initialContract);
		
		notificationService.sendNotification(
			owner, 
			"new initial contract is requested from "+ seeker.getName() +" please check out your initial contracts"
		);
		
		return new InitialContractCreationResponse(initialContract.getId());
	}

	public void acceptContract(long id) {
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		if(contract.getStatus().equals(InitialContractStatus.REJECT)) {
			throw new ConstraintViolationException("this contract is already rejected",Set.of());
		}
		List<InitialContract> propertyContracts = initialContractRepository
				.findByPropertyId(contract.getProperty_id());
		for(InitialContract propertyContract : propertyContracts) {
			if(propertyContract.getStatus().equals(InitialContractStatus.PENDING_PROCESSING) 
					|| propertyContract.getStatus().equals(InitialContractStatus.UNDER_PROCESS))
				throw new ConstraintViolationException("you can't approve this contract", Set.of());
		}
		changeContractStatus(
				contract,
				InitialContractStatus.PENDING_PROCESSING,
				PropertyStatus.PENDING_PROCESSING
		);
	}

	public void rejectContract(long id) {
		InitialContract contract = initialContractRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("contract not found")
		);
		changeContractStatus(
				contract,
				InitialContractStatus.REJECT,
				PropertyStatus.AVAILABLE
		);
	}
	
	@Transactional
	public void changeContractStatus(InitialContract contract, 
									InitialContractStatus contractStatus, 
									PropertyStatus propertyStatus
	) {
		if(contract.getOwner().getId() != JwtService.getCurrentUser().getId()) {
			throw new AccessDeniedException("not authroize to do this process");
		}
		
		Property property = propertyRepository.findById(contract.getProperty_id()).orElseThrow(
				() -> new ResourceNotFoundException("property not found")
		);

		contract.setStatus(contractStatus);
		
		property.setStatus(propertyStatus);
		
		propertyRepository.save(property);
		
		initialContractRepository.save(contract);
	}

}
