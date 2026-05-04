package com.disougie.app_user.lawyer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.intial_contract.initialContractResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/lawyer/initial-contracts")
@RequiredArgsConstructor
@SecurityRequirement(
		name = "BearerAuth"
)
public class LawyerController {
	
	private final LawyerService lawyerService;
	
	@GetMapping
	public ResponseEntity<List<initialContractResponse>> getPendingContracts(){
		List<initialContractResponse> response = lawyerService.getPendingContracts();
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<initialContractResponse> getContract(@PathVariable long id){
		initialContractResponse response = lawyerService.getContract(id);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("my-contracts")
	public ResponseEntity<List<initialContractResponse>> getMyContracts(){
		List<initialContractResponse> response = lawyerService.getMyContracts();
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("{id}/working-on")
	public ResponseEntity<?> workingOnContract(@PathVariable long id){
		lawyerService.workingOnContract(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("{id}/complete")
	public ResponseEntity<?> completeContract(@PathVariable long id){
		lawyerService.completeContract(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("{id}/cancel")
	public ResponseEntity<?> cancelContract(@PathVariable long id){
		lawyerService.cancelContract(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("{id}/ban")
	public ResponseEntity<?> banContract(@PathVariable long id){
		lawyerService.banContract(id);
		return ResponseEntity.noContent().build();
	}
	
}
