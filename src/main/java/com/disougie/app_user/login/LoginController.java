package com.disougie.app_user.login;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/login")
@RequiredArgsConstructor
public class LoginController {
	
	private final LoginService loginService;
	
	@PostMapping
	public ResponseEntity<LoginResponse> login(
									@Validated @RequestBody LoginRequest loginRequest,
									HttpServletResponse httpServletResponse){
		LoginResponse response = loginService.login(loginRequest,httpServletResponse);
		return ResponseEntity.status(HttpStatus.OK).body(response);	
	}

}
