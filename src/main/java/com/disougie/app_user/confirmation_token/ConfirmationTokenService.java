package com.disougie.app_user.confirmation_token;

import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.VERIFY_ROUTING_KEY;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.app_user.change_info.ChangeEmail;
import com.disougie.app_user.change_info.ChangeEmailRepository;
import com.disougie.email.EmailRequest;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.util.TimeUtil;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {
	
	private final ConfirmationTokenRepository confirmationTokenRepository;
	private final AppUserRepository appUserRepository;
	private final ChangeEmailRepository changeEmailRepository;
	private final RabbitTemplate rabbitTemplate;
	
	public ConfirmationToken generateConfirmationToken(AppUser user) {
		
		String tokenString = UUID.randomUUID().toString();
		
		ConfirmationToken token = new ConfirmationToken(tokenString,user);
		
		return confirmationTokenRepository.save(token);
		
	}
	
	@Hidden
	@Transactional
	public void verifyToken(String tokenSting) {
		
		ConfirmationToken token = confirmationTokenRepository.findByToken(tokenSting).orElseThrow(
				() -> new ResourceNotFoundException("Token not found")
		);
		
		if(LocalDateTime.now().isAfter(token.getExpireAt())) {
			throw new ConstraintViolationException(
					"This token has been expired ", Set.of()
			);
		}
		
		token.setConfirmedAt(LocalDateTime.now());
		confirmationTokenRepository.save(token);
		
		AppUser user = token.getUser();
		user.setEnabled(true);
		appUserRepository.save(user);
		
	}

	public void resendToken(ResendTokenRequest request) {
		
		AppUser user =  appUserRepository.findByEmail(request.email()).orElseThrow(
				() -> new ResourceNotFoundException("User not found")
		);
		
		if(user.isEnabled()) {
			throw new ConstraintViolationException(
					"you alreay have an active email",Set.of()
			);
		}
		
		Optional<ConfirmationToken> confirmationTokenOptional = confirmationTokenRepository.findByAppUser(user);
				
		if(confirmationTokenOptional.isPresent()) {
			
			ConfirmationToken confirmationToken = confirmationTokenOptional.get();
			
			if(confirmationToken.getExpireAt().isAfter(LocalDateTime.now())) {
				throw new ConstraintViolationException(
						"you alreay have an active verification token, please check your email",Set.of()
				);
			}
			else {
				LocalDateTime now = LocalDateTime.now();
				
				confirmationToken.setIssuedAt(now);
				confirmationToken.setExpireAt(now.plusMinutes(15));
				confirmationTokenRepository.save(confirmationToken);
				
//				emailSender.sendEmailVerfication(user, confirmationToken.getToken());
				
				EmailRequest emailRequest = new EmailRequest(
						user.getName(), user.getEmail(), confirmationToken.getToken()
				);
				rabbitTemplate.convertAndSend(
						EMAIL_EXCHANGE, VERIFY_ROUTING_KEY, emailRequest
				);
			}
				
		}	
				
	}

	public void verifyChangeEmail(String token) {
		
		ConfirmationToken confirmationToken = confirmationTokenRepository
				.findByToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("token not found"));
		
		if(confirmationToken.getExpireAt().isBefore(TimeUtil.now())) {
			throw new ConstraintViolationException(
					"this token has been expired", Set.of()
			);
		}

		ChangeEmail changeEmail = changeEmailRepository
				.findByConfirmationToken(confirmationToken)
				.orElseThrow(() -> new ResourceNotFoundException("change request not found"));
		
		AppUser user = changeEmail.getUser();
		user.setEmail(changeEmail.getNewEmail());
		user.setEnabled(false);
		appUserRepository.save(user);
		
//		emailSender.sendEmailVerfication(user, token);
		EmailRequest emailRequest = new EmailRequest(
				user.getName(), user.getEmail(), token
		);
		rabbitTemplate.convertAndSend(
				EMAIL_EXCHANGE, VERIFY_ROUTING_KEY, emailRequest
		);
		
	}

}
