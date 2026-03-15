package com.disougie.intial_contract;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/initial-contracts")
@RequiredArgsConstructor
public class InitialContractController {

	private final InitialContractService initialContractService;

	@GetMapping
	public ResponseEntity<List<initialContractResponse>> getMyInitialContracts(){
		List<initialContractResponse> response = initialContractService.getMyInitialContracts();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<initialContractResponse> getInitialContract(
					@PathVariable Long id
	){
		initialContractResponse response = initialContractService.getInitialContract(id);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping
	public ResponseEntity<InitialContractCreationResponse> createInitialContract(
					@Validated @RequestBody InitialContractCreationRequest request
	){	
		InitialContractCreationResponse response = initialContractService
				.createInitialContract(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping("{id}/accept")
	public ResponseEntity<?> acceptContract(@PathVariable long id){
		initialContractService.acceptContract(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("{id}/reject")
	public ResponseEntity<?> rejectContract(@PathVariable long id){
		initialContractService.rejectContract(id);
		return ResponseEntity.noContent().build();
	}
	
}
