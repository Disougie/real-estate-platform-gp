package com.disougie.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.disougie.app_user.AppUser;
import com.disougie.security.JwtService;
import com.disougie.util.PageResponse;
import com.disougie.util.PageResponseMapper;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PageResponseMapper<NotificationResponse> pageResponseMapper;

    @InjectMocks
    private NotificationService notificationService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser recipient;
    private Notification notification;

    @BeforeEach
    void setUp() {
        recipient = AppUser.builder().id(1L).email("user@test.com").build();
        notification = new Notification(1L, recipient, "Test message", null);

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @SuppressWarnings("unchecked")
	@Test
    @DisplayName("Should return paged notifications for current user")
    void getMyNotifications_ShouldReturnPageResponse() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(recipient);
        
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByRecipient(eq(recipient), any(PageRequest.class))).thenReturn(notificationPage);

        PageResponse<NotificationResponse> expectedResponse = new PageResponse<>(
                List.of(new NotificationResponse("Test message")), 0, 1, 1, 1, true
        );
        when(pageResponseMapper.apply(any(Page.class))).thenReturn(expectedResponse);

        // When
        PageResponse<NotificationResponse> result = notificationService.getMyNotifications(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Test message", result.content().get(0).message());
        verify(notificationRepository).findByRecipient(eq(recipient), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should send notification and dispatch WebSocket message")
    void sendNotification_ShouldSaveAndSendWebSocketMessage() {
        // Given
        String message = "Hello World";

        // When
        notificationService.sendNotification(recipient, message);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq(recipient.getUsername()),
                eq("/queue/notifications"),
                any(NotificationResponse.class)
        );
    }
}
