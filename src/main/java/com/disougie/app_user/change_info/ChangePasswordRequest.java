package com.disougie.app_user.change_info;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
		@NotBlank String oldPassword,
		@NotBlank String newPassword
) {

}
