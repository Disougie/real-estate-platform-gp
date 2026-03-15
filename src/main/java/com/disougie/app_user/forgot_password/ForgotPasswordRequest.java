package com.disougie.app_user.forgot_password;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank String email) {

}
