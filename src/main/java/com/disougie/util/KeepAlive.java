package com.disougie.util;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.app_user.AppUserRepository;
import com.disougie.property.PropertyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RestController
@RequiredArgsConstructor
public class KeepAlive {
	
	private final AppUserRepository appUserRepository;
	private final PropertyRepository propertyRepository;
	
	@Scheduled(fixedRate = 12 * 60 * 60 * 1000)
	public void keepMysqlAlive() {
	    appUserRepository.keepAlive();
	}
	
	@Scheduled(fixedRate = 12 * 60 * 60 * 1000)
	public void keepMongodbAlive() {
	    propertyRepository.keepAlive(PageRequest.of(0, 1));
	}
	
	@GetMapping("keep-alive")
	public ResponseEntity<?> keepServerAlive() {
		return ResponseEntity.ok(Map.of("Status", "Alive"));
	}

}
