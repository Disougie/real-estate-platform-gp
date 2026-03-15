package com.disougie.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	@Value("${jwt.secret}")
	private String Key;
	
	public SecretKey secretKey() {
		return Keys.hmacShaKeyFor(Key.getBytes());
	}
	
	public static AppUser getCurrentUser() {
		return (AppUser)SecurityContextHolder
					.getContext()
					.getAuthentication()
					.getPrincipal();
	}
	
	public String generateJwtToken(Authentication authenticatedUser) {
		
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expireAt = now.plusMinutes(30);
		
		Date issuedDate = Date.from(
				now.atZone(ZoneId.systemDefault()).toInstant()
		);
		
		Date expirationDate = Date.from(
				expireAt.atZone(ZoneId.systemDefault()).toInstant()
		);
		
		AppUser user = (AppUser) authenticatedUser.getPrincipal();
		
		String jws = Jwts.builder()
				.issuedAt(issuedDate)
				.expiration(expirationDate)
				.subject(user.getEmail())
				.claims()
					.add("authorities", user.getAuthorities())
					.and()
				.signWith(secretKey())
				.compact();
		
		String token = "Bearer " + jws;
		
		return token;
	}
	
	private Claims parseJwtToken(String token) {
		
		return Jwts.parser()
				.verifyWith(secretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		
	}
	
	public String extractUsername(String token) {
		return parseJwtToken(token)
				.get("sub", String.class);
	}
	
	public boolean isValidJwtToken(String token, UserDetails userDetails) {
		
		Claims payload = parseJwtToken(token);
		
		Date expiration = payload.getExpiration();
		
		Date now = Date.from(
				LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
		);
		
		if(now.after(expiration)) {
			throw new AuthenticationException("Time out please login again") {};
		}
			
		String username = payload.get("sub", String.class);
		
		if(!userDetails.getUsername().equals(username)) {
			throw new AuthenticationException("invalid jwt token") {};
		}
			
		return true;
			
	}

}
