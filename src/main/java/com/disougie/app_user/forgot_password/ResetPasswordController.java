package com.disougie.app_user.forgot_password;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("api/v1/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {
	
	private final PasswordService passwordService;
	
	@GetMapping
	public String resetPage(@RequestParam String token, Model model) {
		model.addAttribute("token", token);
		return "reset";
	}
	
	@PostMapping
	@ResponseBody
	public ResponseEntity<Void> resetPassword(
							/*	@Validated @RequestBody */ ResetPasswordRequest request){
		String redirectWebsite = passwordService.resetPassword(request);
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(redirectWebsite))
				.build();
	}
	
}
