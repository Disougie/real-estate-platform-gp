package com.disougie.app_user.confirmation_token;

import java.time.LocalDateTime;

import com.disougie.app_user.AppUser;
import com.disougie.util.TimeUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ConfirmationToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String token;
	
	@Column(nullable = false)
	private LocalDateTime issuedAt;
	
	@Column(nullable = false)
	private LocalDateTime expireAt;
	
	private LocalDateTime confirmedAt;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser user;
	
	public ConfirmationToken(String token, AppUser user) {
		this.token = token;
		this.user = user;
		this.issuedAt = TimeUtil.now();
		this.expireAt = this.issuedAt.plusMinutes(15);
	}

}
