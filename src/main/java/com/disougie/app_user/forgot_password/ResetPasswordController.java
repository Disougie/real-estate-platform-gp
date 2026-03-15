package com.disougie.app_user.forgot_password;

import java.util.Map;

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
	public ResponseEntity<Map<String, String>> resetPassword(
							/*	@Validated @RequestBody */ ResetPasswordRequest request){
		passwordService.resetPassword(request);
		return ResponseEntity.ok(Map.of("message","password change succfully"));
	}
	
}
