package com.disougie.app_user.registration;

import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.VERIFY_ROUTING_KEY;

import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.AppUserRole;
import com.disougie.app_user.confirmation_token.ConfirmationToken;
import com.disougie.app_user.confirmation_token.ConfirmationTokenService;
import com.disougie.email.EmailRequest;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {
	
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final ConfirmationTokenService confirmationTokenService;
	private final RabbitTemplate rabbitTemplate;
	
	public RegistrationResponse registerUser(RegistrationRequest request) {
		return registerUser(request, AppUserRole.USER);
	}
	
	@Transactional
	public RegistrationResponse registerUser(RegistrationRequest request, AppUserRole role) {
		
		if(appUserRepository.findByEmailIncludingDeleted(request.email()).isPresent()) {
			throw new ConstraintViolationException("Email already taken",Set.of());
		}
		
		if(!request.password().equals(request.confirmPassword())) {
			throw new ConstraintViolationException(
					"The password does not match it's confimation", Set.of()
			);
		}
		
		AppUser user = AppUser.builder()
				.name(request.name())
				.phone(request.phone())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.role(role)
				.enabled(false)
				.build();
		
		AppUser savedUser = appUserRepository.save(user);
				
		ConfirmationToken confirmationToken = confirmationTokenService
				.generateConfirmationToken(savedUser);
		
		EmailRequest emailRequest = new EmailRequest(
				savedUser.getName(), savedUser.getEmail(), confirmationToken.getToken()
		);
		rabbitTemplate.convertAndSend(
				EMAIL_EXCHANGE, VERIFY_ROUTING_KEY, emailRequest
		);
		
		return new RegistrationResponse(savedUser.getId());
	}

}
