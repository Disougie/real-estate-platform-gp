package com.disougie.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.disougie.app_user.AppUser;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;
import com.disougie.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationRepository notificationRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final PageResponseMapper<NotificationResponse> pageResponseMapper;

	public PageResponse<NotificationResponse> getMyNotifications(int page, int size) {
		 
		AppUser user = JwtService.getCurrentUser();
		
		Page<NotificationResponse> pageOfNotifications = notificationRepository
				.findByRecipient(user, PageRequest.of(page, size))
				.map(note -> new NotificationResponse(note.getMessage()));
		
		return pageResponseMapper.apply(pageOfNotifications);
		
	}
	
	public void sendNotification(AppUser recipient, String message) {
		notificationRepository.save(
				new Notification(null,recipient,message,TimeUtil.now())
		);
		messagingTemplate.convertAndSendToUser(
				recipient.getUsername(),
				"/queue/notifications",
				new NotificationResponse(message)
		);
	}
}
