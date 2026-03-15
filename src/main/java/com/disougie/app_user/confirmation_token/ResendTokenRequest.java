package com.disougie.app_user.confirmation_token;

import jakarta.validation.constraints.Email;

public record ResendTokenRequest(@Email String email) {

}
