package com.disougie.app_user;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@SecurityRequirement(
		name = "BearerAuth"
)
public class AppUserController {
	
	private final AppUserService appUserService;
	private final DisableUserService disableUserService;
	
	@GetMapping("{id}")
	public ResponseEntity<AppUserResponse> getUser(@PathVariable Long id){
		AppUserResponse userResponse = appUserService.getUser(id);
		return ResponseEntity.ok(userResponse);
	}
	
	@DeleteMapping
	public ResponseEntity<?> disableAccount(@Validated @RequestBody DisableRequest request){
		disableUserService.disableAccount(request);
		return ResponseEntity.noContent().build();
	}
	
}
