package com.disougie.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import com.rabbitmq.client.Channel;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Channel channel;

    @InjectMocks
    private EmailService emailService;

    private Message message;
    private MessageProperties messageProperties;
    private EmailRequest request;

    @BeforeEach
    void setUp() {
        message = mock(Message.class);
        messageProperties = new MessageProperties();
        messageProperties.setDeliveryTag(100L);
        when(message.getMessageProperties()).thenReturn(messageProperties);

        request = new EmailRequest("John Doe", "john@test.com", "token-123");
    }

    @Test
    @DisplayName("Should successfully send verification email and ack message")
    void sendEmailVerfication_ShouldSendEmailAndAck() throws Exception {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmailVerfication(request, message, channel);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(channel).basicAck(100L, false);
    }

    @Test
    @DisplayName("Should catch exception, increment retry count, requeue, and ack original message on failure")
    void sendEmailVerfication_ShouldRetryOnFailure() throws Exception {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(mimeMessage);

        // When
        emailService.sendEmailVerfication(request, message, channel);

        // Then
        verify(rabbitTemplate).send(
                eq(EmailMQConstant.EMAIL_RETRY_EXCHANGE),
                eq(EmailMQConstant.VERIFY_RETRY_ROUTING_KEY),
                any(Message.class)
        );
        verify(channel).basicAck(100L, false);
    }

    @Test
    @DisplayName("Should reject message if retry count exceeds limit")
    void sendEmailVerfication_ShouldRejectAfterMaxRetries() throws Exception {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(mimeMessage);
        
        messageProperties.setHeader("x-retry-count", 7);

        // When
        emailService.sendEmailVerfication(request, message, channel);

        // Then
        verify(channel).basicReject(100L, false);
    }

    @Test
    @DisplayName("Should successfully send change email confirmation and ack")
    void sendChangeEmailConfirmation_ShouldSendEmailAndAck() throws Exception {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendChangeEmailConfirmation(request, message, channel);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(channel).basicAck(100L, false);
    }

    @Test
    @DisplayName("Should successfully send forgot password verification and ack")
    void sendForgotPasswordVerfication_ShouldSendEmailAndAck() throws Exception {
        // Given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendForgotPasswordVerfication(request, message, channel);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(channel).basicAck(100L, false);
    }
}
