package com.disougie.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ApiError(
		String path,
		HttpStatus status,
		String message, 
		LocalDateTime time
) {}
