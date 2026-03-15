package com.disougie.email;

import static com.disougie.email.EmailMQConstant.CHANGE_DLQ;
import static com.disougie.email.EmailMQConstant.CHANGE_DLQ_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.CHANGE_QUEUE;
import static com.disougie.email.EmailMQConstant.CHANGE_RETRY_QUEUE;
import static com.disougie.email.EmailMQConstant.CHANGE_RETRY_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.CHANGE_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.EMAIL_DLQ_EXCHANGE;
import static com.disougie.email.EmailMQConstant.EMAIL_EXCHANGE;
import static com.disougie.email.EmailMQConstant.EMAIL_RETRY_EXCHANGE;
import static com.disougie.email.EmailMQConstant.MAIN_TTL;
import static com.disougie.email.EmailMQConstant.RESET_DLQ;
import static com.disougie.email.EmailMQConstant.RESET_DLQ_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.RESET_QUEUE;
import static com.disougie.email.EmailMQConstant.RESET_RETRY_QUEUE;
import static com.disougie.email.EmailMQConstant.RESET_RETRY_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.RESET_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.RETRY_TTL;
import static com.disougie.email.EmailMQConstant.VERIFY_DLQ;
import static com.disougie.email.EmailMQConstant.VERIFY_DLQ_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.VERIFY_QUEUE;
import static com.disougie.email.EmailMQConstant.VERIFY_RETRY_QUEUE;
import static com.disougie.email.EmailMQConstant.VERIFY_RETRY_ROUTING_KEY;
import static com.disougie.email.EmailMQConstant.VERIFY_ROUTING_KEY;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailMQConfig {
	
	@Bean
	MessageConverter messageConverter() {
		JacksonJsonMessageConverter converter  = 
				new JacksonJsonMessageConverter();
		DefaultJacksonJavaTypeMapper typeMapper = 
				new DefaultJacksonJavaTypeMapper();
		
		typeMapper.setTrustedPackages(
				"com.disougie.email", 
				"com.disougie.recommendation"
		);
		converter.setJavaTypeMapper(typeMapper);
		return converter;
	}
	
//	ObjectMapper objectMapper() {
//		return JsonMapper
//				.builder()
//				.findAndAddModules()
//				.build();
//	}
	
	@Bean
	Exchange emailExchange() {
		return new TopicExchange(EMAIL_EXCHANGE);
	}
	
	@Bean
	Queue verifyQueue() {
		return QueueBuilder
				.durable(VERIFY_QUEUE)
				.ttl(MAIN_TTL)
				.deadLetterExchange(EMAIL_DLQ_EXCHANGE)
				.deadLetterRoutingKey(VERIFY_DLQ_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Queue changeQueue() {
		return QueueBuilder
				.durable(CHANGE_QUEUE)
				.ttl(MAIN_TTL)
				.deadLetterExchange(EMAIL_DLQ_EXCHANGE)
				.deadLetterRoutingKey(CHANGE_DLQ_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Queue resetQueue() {
		return QueueBuilder
				.durable(RESET_QUEUE)
				.ttl(MAIN_TTL)
				.deadLetterExchange(EMAIL_DLQ_EXCHANGE)
				.deadLetterRoutingKey(RESET_DLQ_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Binding verifyQueueBinding() {
		return BindingBuilder
				.bind(verifyQueue())
				.to(emailExchange())
				.with(VERIFY_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding changeQueueBinding() {
		return BindingBuilder
				.bind(changeQueue())
				.to(emailExchange())
				.with(CHANGE_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding resetQueueBinding() {
		return BindingBuilder
				.bind(resetQueue())
				.to(emailExchange())
				.with(RESET_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Exchange retryExchange() {
		return new DirectExchange(EMAIL_RETRY_EXCHANGE);
	}
	
	@Bean
	Queue verifyRetryQueue() {
		return QueueBuilder
				.durable(VERIFY_RETRY_QUEUE)
				.ttl(RETRY_TTL)
				.deadLetterExchange(EMAIL_EXCHANGE)
				.deadLetterRoutingKey(VERIFY_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Queue changeRetryQueue() {
		return QueueBuilder
				.durable(CHANGE_RETRY_QUEUE)
				.ttl(RETRY_TTL)
				.deadLetterExchange(EMAIL_EXCHANGE)
				.deadLetterRoutingKey(CHANGE_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Queue resetRetryQueue() {
		return QueueBuilder
				.durable(RESET_RETRY_QUEUE)
				.ttl(RETRY_TTL)
				.deadLetterExchange(EMAIL_EXCHANGE)
				.deadLetterRoutingKey(RESET_ROUTING_KEY)
				.build();
	}
	
	@Bean
	Binding verifyRetryQueueBinding() {
		return BindingBuilder
				.bind(verifyRetryQueue())
				.to(retryExchange())
				.with(VERIFY_RETRY_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding changeRetryQueueBinding() {
		return BindingBuilder
				.bind(changeRetryQueue())
				.to(retryExchange())
				.with(CHANGE_RETRY_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding resetRetryQueueBinding() {
		return BindingBuilder
				.bind(resetRetryQueue())
				.to(retryExchange())
				.with(RESET_RETRY_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Exchange dlqExchange() {
		return new DirectExchange(EMAIL_DLQ_EXCHANGE);
	}
	
	@Bean
	Queue verifyDlq() {
		return QueueBuilder
				.durable(VERIFY_DLQ)
				.build();
	}
	
	@Bean
	Queue changeDlq() {
		return QueueBuilder
				.durable(CHANGE_DLQ)
				.build();
	}
	
	@Bean
	Queue resetDlq() {
		return QueueBuilder
				.durable(RESET_DLQ)
				.build();
	}
	
	@Bean
	Binding verifyDlqBinding() {
		return BindingBuilder
				.bind(verifyDlq())
				.to(dlqExchange())
				.with(VERIFY_DLQ_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding changeDlqBinding() {
		return BindingBuilder
				.bind(changeDlq())
				.to(dlqExchange())
				.with(CHANGE_DLQ_ROUTING_KEY)
				.noargs();
	}
	
	@Bean
	Binding resetDlqBinding() {
		return BindingBuilder
				.bind(resetDlq())
				.to(dlqExchange())
				.with(RESET_DLQ_ROUTING_KEY)
				.noargs();
	}
	
}
