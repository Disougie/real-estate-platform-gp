package com.disougie.app_user.login;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
	
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public LoginResponse login(LoginRequest request, HttpServletResponse response) {
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				request.email(),
				request.password()
		);
		
		Authentication authenticatedUser = authenticationManager
				.authenticate(authentication);
		
		String token = jwtService.generateJwtToken(authenticatedUser);
				
		response.setHeader("Authorization", token);
		
		AppUser user = (AppUser) authenticatedUser.getPrincipal();
		
		return new LoginResponse(
				user.getName(),
				user.getRole().getRole(),
				user.getId()
		);
	}

}
