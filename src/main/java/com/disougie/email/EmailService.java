package com.disougie.email;

import static com.disougie.email.EmailMQConstant.CHANGE_RETRY_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.EMAIL_RETRY_EXCHANGE;
import static com.disougie.email.EmailMQConstant.RESET_RETRY_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.VERIFY_RETRY_ROUTING_KEY;

import java.io.IOException;
import java.util.Collections;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import brevo.ApiClient;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
// Removed jakarta.mail imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements EmailSender{
	
	@Value("${brevo.api.key}")
	private String brevoApiKey;
	private final RabbitTemplate rabbitTemplate;
	@Value("${SERVER_URL}")
	private String serverUrl;
	
	private void sendMimeMessage(
			String recipientName,
			String recipientEmail,
			String title,
			String body,
			String link
	) throws Exception {
		
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
		apiKey.setApiKey(brevoApiKey);
		
		TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
		SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
		
		SendSmtpEmailSender sender = new SendSmtpEmailSender();
		sender.setEmail("adisougie@gmail.com");
		sender.setName("منصة عقارات السودان");
		sendSmtpEmail.setSender(sender);
		
		SendSmtpEmailTo to = new SendSmtpEmailTo();
		to.setEmail(recipientEmail);
		to.setName(recipientName);
		sendSmtpEmail.setTo(Collections.singletonList(to));
		
		sendSmtpEmail.setSubject(title);
		sendSmtpEmail.setHtmlContent(buildEmailText(recipientName, link, title, body));
		
		apiInstance.sendTransacEmail(sendSmtpEmail);
		log.info("email sent via Brevo HTTP API");
		
	}
	
	@Override
	@RabbitListener(queues = "#{verifyQueue.name}", ackMode = "MANUAL") 
	public void sendEmailVerfication(
			EmailRequest request,
			Message message, 
			Channel channel
	) throws IOException {
		
		MessageProperties messageProperties = message.getMessageProperties();
		try {
			String link = serverUrl + "/api/v1/token/verify?token=" + request.getToken();
			
			sendMimeMessage(
					request.getRecipientName(),
					request.getRecipientEmail(),
					"تاكيد البريد الاكتروني",
					"شكراً لتسجيلك معانا! قبل نكمل باقي الخطوات، لازم تأكّد لينا بريدك الإلكتروني.",
					link
			);
			
			channel.basicAck(messageProperties.getDeliveryTag(), false);
		}
		catch (Exception e) {
			int retryCount = (Integer)messageProperties.getHeaders()
					.getOrDefault("x-retry-count", 0);
			retryCount += 1;
			if(retryCount >= 7) {
				channel.basicReject(messageProperties.getDeliveryTag(), false);
				log.error("email sending failed");
				return;
			}
			messageProperties.setHeader("x-retry-count", retryCount);
			rabbitTemplate.send(
					EMAIL_RETRY_EXCHANGE, 
					VERIFY_RETRY_ROUTING_KEY,
					MessageBuilder.fromMessage(message).build()
			);
			channel.basicAck(messageProperties.getDeliveryTag(), false);
			log.warn("email sending failed but trying to send it again");
			e.printStackTrace();
		} 
		
	}
	
	@Override
	@RabbitListener(queues = "#{changeQueue.name}", ackMode = "MANUAL") 
	public void sendChangeEmailConfirmation(
			EmailRequest request,
			Message message,
			Channel channel
	) throws IOException {
		
		MessageProperties messageProperties = message.getMessageProperties();
		
		try {
			String link = serverUrl+"/api/v1/token/verify-change?token=" + request.getToken();
			
			sendMimeMessage(
					request.getRecipientName(),
					request.getRecipientEmail(),
					"تاكيد تغيير البريد الاكتروني",
					"لو اطلبت تغيير البريد الالكتروني اضغط على الرابط لتأكيد هويتك وبعد دذلك ادخل الايميل الجديد واكده ايضا , اما لو لم تطلب شئ يمكن تجاهل الرسالة بأمان",
					link
			);
			
			channel.basicAck(messageProperties.getDeliveryTag(), false);
		} 
		catch (Exception e) {
			int retryCount = (Integer)messageProperties.getHeaders()
					.getOrDefault("x-retry-count", 0);
			retryCount += 1;
			if(retryCount >= 7) {
				channel.basicReject(messageProperties.getDeliveryTag(), false);
				return;
			}
			messageProperties.setHeader("x-retry-count", retryCount);
			rabbitTemplate.send(
					EMAIL_RETRY_EXCHANGE, 
					CHANGE_RETRY_ROUTING_KEY,
					MessageBuilder.fromMessage(message).build()
			);
			channel.basicAck(messageProperties.getDeliveryTag(), false);
			log.warn("email sending failed but trying to send it again");
			e.printStackTrace();
		}
		
	}
	
	@Override
	@RabbitListener(queues = "#{resetQueue.name}", ackMode = "MANUAL") 
	public void sendForgotPasswordVerfication(
			EmailRequest request, 
			Message message, 
			Channel channel
	) throws IOException {
		
		MessageProperties messageProperties = message.getMessageProperties();

		try {
			String link = serverUrl+"/api/v1/reset-password?token=" + request.getToken();
			
			sendMimeMessage(
					request.getRecipientName(),
					request.getRecipientEmail(),
					"نسيان كلمة السر",
					"اضغط على الرابط اذا انت طلبت اعادة تعيين كلمة السر بسبب النسيان, لو لم تطلبها يمكنك تجاهل الرسالة بأمان",
					link
			);
			
			channel.basicAck(messageProperties.getDeliveryTag(), false);
			
		} catch (Exception e) {
			int retryCount = (Integer)messageProperties.getHeaders()
					.getOrDefault("x-retry-count", 0);
			retryCount += 1;
			if(retryCount >= 7) {
				channel.basicReject(messageProperties.getDeliveryTag(), false);
				return;
			}
			messageProperties.setHeader("x-retry-count", retryCount);
			rabbitTemplate.send(
					EMAIL_RETRY_EXCHANGE, 
					RESET_RETRY_ROUTING_KEY,
					MessageBuilder.fromMessage(message).build()
			);
			channel.basicAck(messageProperties.getDeliveryTag(), false);
			log.warn("email sending failed but trying to send it again");
			e.printStackTrace();
		}
		
	}
	
	private String buildEmailText(
			String name, 
			String link, 
			String title, 
			String content
	) throws IOException {
		
		String template = """
				<!DOCTYPE html>
				<html lang="ar">
				<head>
				    <meta charset="UTF-8" />
				    <title>{{title}}</title>
				</head>
				<body style="font-family: Arial, sans-serif; background-color:#f5f5f5; padding: 20px; direction: rtl;">
				    <table align="center" width="100%" cellpadding="0" cellspacing="0"
				           style="max-width: 600px; background:#ffffff; border-radius:8px; padding:25px; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
				        <tr>
				            <td style="text-align: center;">
				                <h2 style="color:#333; margin-bottom:10px;">{{title}}</h2>
				
				                <p style="color:#555; margin-top:0;">
				                    السلام عليكم <strong>{{username}}</strong> 👋،<br>
				                    {{content}}
				                </p>
				
				                <a href="{{confirmation_link}}"
				                   style="display:inline-block; margin-top:20px; padding:12px 20px;
				                          background-color:#4CAF50; color:white; text-decoration:none;
				                          border-radius:5px; font-size:16px;">
				                    اضغط هنا للتأكيد
				                </a>
				
				                <p style="color:#777; margin-top:25px; font-size:14px;">
				                    لو الزر ما اشتغل معاك، انسخ اللينك تحت ده وافتحو في المتصفح:
				                </p>
				
				                <p style="word-break: break-all; color:#0066cc; font-size:14px;">
				                    {{confirmation_link}}
				                </p>
				
				                <p style="color:#aaa; margin-top:30px; font-size:12px;">
				                    © 2025 منصة العقارات — جميع الحقوق محفوظة.
				                </p>
				            </td>
				        </tr>
				    </table>
				</body>
				</html>
				""";
		
		String html = template
				.replace("{{username}}", name)
				.replace("{{confirmation_link}}", link)
				.replace("{{title}}", title)
				.replace("{{content}}", content);
		
		return html;
	}

}
