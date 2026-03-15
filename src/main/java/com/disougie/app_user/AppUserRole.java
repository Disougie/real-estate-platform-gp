package com.disougie.app_user;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum AppUserRole {
	
	USER("USER"),
	ADMIN("ADMIN"),
	LAWYER("LAWYER");
	
	private final String role;
	
	private AppUserRole(String role) {
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}

	public Set<GrantedAuthority> getAuthorities(){
		return Set.of(new SimpleGrantedAuthority("ROLE_" + this.role));
	}
}
