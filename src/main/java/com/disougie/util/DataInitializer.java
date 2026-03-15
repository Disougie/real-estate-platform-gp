package com.disougie.util;

import static com.disougie.app_user.AppUserRole.ADMIN;
import static com.disougie.app_user.AppUserRole.USER;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
	
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		
		if(appUserRepository.count() == 0) {
			
			List<AppUser> defaultUsers = List.of(
					
					AppUser.builder()
						.name("admin")
						.email("admin@system.com")
						.password(passwordEncoder.encode("admin"))
						.role(ADMIN)
						.phone("0123456789")
						.enabled(true)
						.build(),
						
					AppUser.builder()
						.name("احمد محمد احمد محمد")
						.email("adisougie@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("عبيد عثمان عبيد الاسد")
						.email("ebaid@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("محمد مدثر عبدالمطلب عبد المجيد")
						.email("mohamed@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("احمد صلاح احمد محمد")
						.email("ِahmed@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("علي طه محمد احمد")
						.email("ali@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("خالد احمد محمد احمد")
						.email("khalid@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("عمر احمد محمد احمد")
						.email("omer@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("عثمان طه محمد احمد")
						.email("osman@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build(),
						
					AppUser.builder()
						.name("نوح عبدالغني عبدالله الشيخ")
						.email("nooh@gmail.com")
						.password(passwordEncoder.encode("password"))
						.role(USER)
						.enabled(true)
						.phone("0123456789")
						.build()
					
			);
			
			appUserRepository.saveAll(defaultUsers);
		}
		
	}

}
