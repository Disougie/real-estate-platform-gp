package com.disougie.app_user.change_info;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/change")
@RequiredArgsConstructor
public class ChangeInfoController {
	
	private final ChangeInfoService changeInfoService;
	
	@PostMapping("email")
	public ResponseEntity<?> changeEmail(
								@Validated @RequestBody ChangeEmailRequest request){
		changeInfoService.changeEmail(request);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("password")
	public ResponseEntity<?> changePassword(
								@Validated @RequestBody ChangePasswordRequest request){
		changeInfoService.changePassword(request);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("phone")
	public ResponseEntity<?> changePhone(
								@Validated @RequestBody ChangePhoneRequest request){
		changeInfoService.changePhone(request);
		return ResponseEntity.noContent().build();
	}
	
}
