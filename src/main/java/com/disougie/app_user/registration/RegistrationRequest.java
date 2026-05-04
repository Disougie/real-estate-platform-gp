package com.disougie.app_user.registration;

import com.disougie.util.Password;
import com.disougie.util.PhoneNumber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
		@NotBlank
		String name,
		@PhoneNumber
		String phone,
		@Email
		String email,
		@Password
		String password,
		@Password
		String confirmPassword
) {
	
}
