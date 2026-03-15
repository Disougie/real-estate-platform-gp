package com.disougie.app_user;

import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class AppUserResponseMapper implements Function<AppUser, AppUserResponse> {

	@Override
	public AppUserResponse apply(AppUser user) {
		return new AppUserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				user.isEnabled(),
				user.getDeletedAt()
		);
	}

}
