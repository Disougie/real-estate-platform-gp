package com.disougie.app_user;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class AppUserController {
	
	private final DisableUserService disableUserService;

	@DeleteMapping
	public ResponseEntity<?> disableAccount(@Validated @RequestBody DisableRequest request){
		disableUserService.disableAccount(request);
		return ResponseEntity.noContent().build();
	}
	
}
