package com.disougie.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.disougie.util.PageResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(
		name = "BearerAuth"
)
public class NotificationController {
	
	private final NotificationService notificationService;
	
	@GetMapping
	public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
										@RequestParam(defaultValue = "0") int page,
										@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<NotificationResponse> response = notificationService
				.getMyNotifications(page,size);
		return ResponseEntity.ok(response);
	}

}
