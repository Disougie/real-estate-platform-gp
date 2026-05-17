package com.disougie.app_user.confirmation_token;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/token")
@RequiredArgsConstructor
public class ConfirmationTokenController {	
	
	private final ConfirmationTokenService tokenService;
	
	@GetMapping("verify")
	public ResponseEntity<Void> verifyToken(@Validated @RequestParam String token){
		String redirectWebsite = tokenService.verifyToken(token);
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(redirectWebsite))
				.build();
	}
	
	@PostMapping("resend")
	public ResponseEntity<?> resendToken(@RequestBody ResendTokenRequest request){
		tokenService.resendToken(request);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("verify-change")
	public ResponseEntity<Void> verifyChangeEmail(@RequestParam String token){
		String redirectWebsite = tokenService.verifyChangeEmail(token);
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(redirectWebsite))
				.build();
	}
}
