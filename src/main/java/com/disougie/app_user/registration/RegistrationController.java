package com.disougie.app_user.registration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/registration")
@RequiredArgsConstructor
public class RegistrationController {
	
	private final RegistrationService registrationService;
	
	@PostMapping
	public ResponseEntity<RegistrationResponse> register(@Validated @RequestBody RegistrationRequest request){
		RegistrationResponse response =registrationService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
