package com.disougie.app_user.change_info;

import java.time.LocalDateTime;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.confirmation_token.ConfirmationToken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ChangeEmail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "user_id", unique = true)
	private AppUser user;
	
	@Column(nullable = false)
	private String newEmail;
	
	@Column(nullable = false)
	private LocalDateTime updatedAt;
	
	@OneToOne
	@JoinColumn(name = "token_id", nullable = false)
	private ConfirmationToken confirmationToken;
	
}
