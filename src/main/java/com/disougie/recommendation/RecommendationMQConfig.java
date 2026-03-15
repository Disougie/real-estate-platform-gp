package com.disougie.recommendation;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationMQConfig {
	
	public static final String RECOMMENDATION_EXCHANGE = "recommendation.events.direct" ;
	public static final String RECOMMENDATION_QUEUE = "recommendation.send.queue";
	public static final String RECOMMENDATION_ROUTING_KEY = "recommendation.send";

	@Bean
	Exchange recommendationExchange() {
		return new DirectExchange(RECOMMENDATION_EXCHANGE);
	}
	
	@Bean
	Queue recommmendationQueue() {
		return QueueBuilder
				.durable(RECOMMENDATION_QUEUE)
				.ttl(60)
				.build();
	}
	
	@Bean
	Binding recommendationBinding() {
		return BindingBuilder
				.bind(recommmendationQueue())
				.to(recommendationExchange())
				.with(RECOMMENDATION_ROUTING_KEY)
				.noargs();
	}
}
