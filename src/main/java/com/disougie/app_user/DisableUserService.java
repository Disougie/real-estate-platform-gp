package com.disougie.app_user;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.security.JwtService;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisableUserService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final PropertyRepository propertyRepository;

	public void disableAccount(DisableRequest request) {
		AppUser user = JwtService.getCurrentUser();
		if(!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ConstraintViolationException("password not correct", Set.of());
		}
		
		List<Property> properties = propertyRepository.findByOwnerId(user.getId());
		
		for(Property property : properties) {
			propertyRepository.delete(property);
		}
		
		appUserRepository.delete(user);
	}

}
