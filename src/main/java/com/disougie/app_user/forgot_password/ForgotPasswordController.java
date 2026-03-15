package com.disougie.app_user.forgot_password;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
	
	private final PasswordService passwordService;
	
	@PostMapping
	public ResponseEntity<Map<String, String>> forgotPassword(
								@Validated @RequestBody ForgotPasswordRequest request){
		passwordService.forgotPassword(request);
		return ResponseEntity.ok(
				Map.of("message", "if the email is correct you will recieve a token")
		);
	}
	
}
