package com.disougie.app_user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.disougie.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService{
	
	private final AppUserRepository appUserRepository;
	private final AppUserResponseMapper appUserResponseMapper;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return appUserRepository.findByEmail(email).orElseThrow(
				() -> new UsernameNotFoundException("This email is not registered")
		);
	}
	
	public AppUserResponse getUser(Long id) {
		return appUserRepository
				.findById(id)
				.map(appUserResponseMapper)
				.orElseThrow(() -> new ResourceNotFoundException("user not found"));
	}
		
}
