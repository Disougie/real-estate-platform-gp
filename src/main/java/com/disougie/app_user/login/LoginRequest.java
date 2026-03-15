package com.disougie.app_user.login;

import jakarta.validation.constraints.Email;

public record LoginRequest(
		@Email
		String email,
		String password
) {
	
}
