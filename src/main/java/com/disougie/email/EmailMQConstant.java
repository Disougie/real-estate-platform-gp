package com.disougie.email;

public final class EmailMQConstant {
	
	public static final String EMAIL_EXCHANGE = "email.events.topic";
	
	public static final int MAIN_TTL = 60000;
	
	public static final String VERIFY_QUEUE = "verify.send.queue";
	public static final String CHANGE_QUEUE = "change.send.queue";
	public static final String RESET_QUEUE = "reset.send.queue";
	
	public static final String VERIFY_ROUTING_KEY = "email.verify";
	public static final String CHANGE_ROUTING_KEY = "email.change";
	public static final String RESET_ROUTING_KEY = "email.reset";
	
	
	public static final String EMAIL_RETRY_EXCHANGE = "email.retry.direct";
	
	public static final int RETRY_TTL = 20000;
	
	public static final String VERIFY_RETRY_QUEUE = "verify.send.queue.retry";
	public static final String CHANGE_RETRY_QUEUE = "change.send.queue.retry";
	public static final String RESET_RETRY_QUEUE = "reset.send.queue.retry";
	
	public static final String VERIFY_RETRY_ROUTING_KEY = "emaiil.verify.retry";
	public static final String CHANGE_RETRY_ROUTING_KEY = "emaiil.change.retry";
	public static final String RESET_RETRY_ROUTING_KEY = "emaiil.reset.retry";
	
	
	public static final String EMAIL_DLQ_EXCHANGE = "email.dlq.direct";
	
	public static final String VERIFY_DLQ = "verify.send.queue.dlq";
	public static final String CHANGE_DLQ = "change.send.queue.dlq";
	public static final String RESET_DLQ = "reset.send.queue.dlq";
	
	public static final String VERIFY_DLQ_ROUTING_KEY = "emaiil.verify.dlq";
	public static final String CHANGE_DLQ_ROUTING_KEY = "emaiil.change.dlq";
	public static final String RESET_DLQ_ROUTING_KEY = "emaiil.reset.dlq";
	
}
