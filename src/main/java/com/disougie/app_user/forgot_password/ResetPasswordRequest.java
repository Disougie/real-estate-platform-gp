package com.disougie.app_user.forgot_password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
		@NotBlank
		String token,
		@Size(min = 8, max = 32)
		String newPassword,
		@Size(min = 8, max = 32)
		String confirmPassword
) {

}
