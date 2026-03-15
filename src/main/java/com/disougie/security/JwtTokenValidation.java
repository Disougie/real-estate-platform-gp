package com.disougie.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenValidation extends OncePerRequestFilter{
	
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, 
									HttpServletResponse response, 
									FilterChain filterChain)
											throws ServletException, IOException {
		
		String berearToken = request.getHeader("Authorization");
		
		if(berearToken == null || berearToken.isBlank() || !berearToken.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String token = berearToken.replace("Bearer ", "");
		
		String email = jwtService.extractUsername(token);
		
		if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			
			if(jwtService.isValidJwtToken(token, userDetails)) {
				
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);
				
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
			}
		}
			
		filterChain.doFilter(request, response);		
		
	}

}
