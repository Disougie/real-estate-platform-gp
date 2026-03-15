package com.disougie.app_user;

import java.time.LocalDateTime;

public record AppUserResponse(
		long id,
		String name,
		String email,
		String phone,
		boolean enabled,
		LocalDateTime deletedAt
) {

}
