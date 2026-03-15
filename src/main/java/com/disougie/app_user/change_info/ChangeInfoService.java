package com.disougie.app_user.change_info;

import static com.disougie.email.EmailMQConstant.CHANGE_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.confirmation_token.ConfirmationToken;
import com.disougie.app_user.confirmation_token.ConfirmationTokenService;
import com.disougie.email.EmailRequest;
import com.disougie.security.JwtService;
import com.disougie.util.TimeUtil;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeInfoService {
	
	private final ChangeEmailRepository changeEmailRepository;
	private final AppUserRepository appUserRepository;
	private final ConfirmationTokenService confirmationTokenService;
	private final PasswordEncoder passwordEncoder;
	private final RabbitTemplate rabbitTemplate;
	
	@Transactional
	public void changeEmail(ChangeEmailRequest request) {
		
		AppUser user =  JwtService.getCurrentUser();
		
		Optional<ChangeEmail> changeEmailOptional = changeEmailRepository
				.findByUser(user);
		
		ChangeEmail changeEmail;
		ConfirmationToken confirmationToken;
		
		LocalDateTime now = TimeUtil.now();
		
		if(changeEmailOptional.isPresent()) {
			changeEmail = changeEmailOptional.get();
			LocalDateTime updated_at = changeEmail.getUpdatedAt();
			if(updated_at.plusDays(1).isAfter(now)) {
				throw new ConstraintViolationException(
						"you can't update your email right now, only once per day allowed try again later", Set.of()
				);
			}
			confirmationToken = confirmationTokenService
					.generateConfirmationToken(user);
			changeEmail.setNewEmail(request.newEmail());
			changeEmail.setUpdatedAt(now.plusDays(1));
			changeEmail.setConfirmationToken(confirmationToken);
		}
		else {
			confirmationToken = confirmationTokenService
					.generateConfirmationToken(user);
			changeEmail = new ChangeEmail(
					null, user, request.newEmail(), now, confirmationToken
			);
		}
		
		changeEmailRepository.save(changeEmail);				
		EmailRequest emailRequest = new EmailRequest(
				user.getName(), user.getEmail(), confirmationToken.getToken()
		);
		rabbitTemplate.convertAndSend(
				EMAIL_EXCHANGE, CHANGE_ROUTING_KEY, emailRequest
		);
	}
	
	public void changePassword(ChangePasswordRequest request) {
		
		AppUser user = JwtService.getCurrentUser();
		
		if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
			throw new ConstraintViolationException("incorrect password", Set.of());
		}
		
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		appUserRepository.save(user);
			
	}

	public void changePhone(ChangePhoneRequest request) {
		
		AppUser user = JwtService.getCurrentUser();
		
		if(!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ConstraintViolationException("incorrect password", Set.of());
		}
		
		user.setPhone(request.phone());
		appUserRepository.save(user);
	}
	
}
