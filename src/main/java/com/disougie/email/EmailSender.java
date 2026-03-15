package com.disougie.email;

import java.io.IOException;

import org.springframework.amqp.core.Message;

import com.rabbitmq.client.Channel;

public interface EmailSender {
	
	void sendEmailVerfication(EmailRequest request, Message message, Channel channel) throws IOException;
	void sendChangeEmailConfirmation(EmailRequest request, Message message, Channel channel) throws IOException;
	void sendForgotPasswordVerfication(EmailRequest request, Message message, Channel channel) throws IOException;

}
