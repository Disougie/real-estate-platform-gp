package com.disougie.util;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.app_user.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RestController
@RequiredArgsConstructor
public class KeepAlive {
	
	private final AppUserRepository appUserRepository;
	
	@Scheduled(fixedRate = 12 * 60 * 60 * 1000)
	public void keepMysqlAlive() {
	    appUserRepository.keepAlive();
	}
	
	@GetMapping("keep-alive")
	public ResponseEntity<?> keepServerAlive() {
		return ResponseEntity.noContent().build();
	}

}
