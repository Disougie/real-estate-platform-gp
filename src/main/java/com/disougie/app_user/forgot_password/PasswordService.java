package com.disougie.app_user.forgot_password;

import static com.disougie.email.EmailMQConstant.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.email.EmailRequest;
import com.disougie.exception.ResourceNotFoundException;
import com.disougie.util.TimeUtil;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {
	
	private final AppUserRepository appUserRepository;
	private final ResetPasswordTokenRepository resetPasswordTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final RabbitTemplate rabbitTemplate;

	public void forgotPassword(ForgotPasswordRequest request) {
		
		Optional<AppUser> userOptional = appUserRepository
				.findByEmail(request.email());
		
		if(!userOptional.isPresent()) {
			return;
		}
		
		AppUser user = userOptional.get();
		String token = UUID.randomUUID().toString();
		LocalDateTime now = TimeUtil.now();
		
		ResetPasswordToken resetToken = new ResetPasswordToken(
				null, 
				user, 
				token,
				now, 
				now.plusMinutes(15),
				false
		);
		
		resetPasswordTokenRepository.save(resetToken);
		
		EmailRequest emailRequest = new EmailRequest(
				user.getName(), user.getEmail(), token
		);
		
		rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, RESET_ROUTING_KEY, emailRequest);
		
	}

	public void resetPassword(ResetPasswordRequest request) {
				
		ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository
				.findByToken(request.token())
				.orElseThrow(() -> new ResourceNotFoundException("token not found"));
		
		if(resetPasswordToken.isUsed()) {
			throw new ConstraintViolationException("this token is already been used", Set.of());
		}
		
		if(resetPasswordToken.getExpireAt().isBefore(TimeUtil.now())) {
			throw new ConstraintViolationException("token expired", Set.of());
		}
		
		if(!request.newPassword().equals(request.confirmPassword())) {
			throw new ConstraintViolationException(
					"the password is not equals its confirmation", Set.of()
			);
		}
		
		AppUser user = resetPasswordToken.getUser();
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		appUserRepository.save(user);
		
		resetPasswordToken.setUsed(true);
		resetPasswordTokenRepository.save(resetPasswordToken);
	}

}
