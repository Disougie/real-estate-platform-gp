package com.disougie.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHandlingController {
		
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handlingUserNotFoundException(
											ResourceNotFoundException e,
											HttpServletRequest request
	){
		ApiError apiError = new ApiError(
				request.getRequestURL().toString(),
				HttpStatus.NOT_FOUND,
				e.getMessage(),
				LocalDateTime.now()
		); 
		
		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handlingConstraintsVioalations(
												ConstraintViolationException e,
												HttpServletRequest request
	){		
		ApiError apiError = new ApiError(
				request.getRequestURI().toString(),
				HttpStatus.BAD_REQUEST,
				e.getMessage(),
				LocalDateTime.now()
		);
		
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handlingMethodArgValidation(
											MethodArgumentNotValidException e,
											HttpServletRequest request
	){
		
		Map<String,String> errors = new HashMap<>();
		
		e.getBindingResult().getFieldErrors()
			.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		
		ApiError apiError = new ApiError(
				request.getRequestURI().toString(),
				HttpStatus.BAD_REQUEST,
				errors.toString(),
				LocalDateTime.now()
		);
		
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handlingAuthenticationException(
											AuthenticationException e,
											HttpServletRequest request
	){	
		ApiError apiError = new ApiError(
				request.getRequestURI().toString(),
				HttpStatus.UNAUTHORIZED,
				e.getMessage(),
				LocalDateTime.now()
		);
		
		return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handlingAccessDeniedException(
													AccessDeniedException e,
													HttpServletRequest request
	){	
		ApiError apiError = new ApiError(
				request.getRequestURI().toString(),
				HttpStatus.FORBIDDEN,
				e.getMessage(),
				LocalDateTime.now()
		);
		
		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ApiError> handlingExpiredJwtException(
												ExpiredJwtException e,
												HttpServletRequest request
	){
		ApiError apiError = new ApiError(
			request.getRequestURI().toString(),
			HttpStatus.FORBIDDEN,
			e.getMessage(),
			LocalDateTime.now()
		);
		
		e.printStackTrace();
		
		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handlingException(
												HttpMessageNotReadableException e,
												HttpServletRequest request
	){
		ApiError apiError = new ApiError(
			request.getRequestURI().toString(),
			HttpStatus.BAD_REQUEST,
			"Http request Not Readable",
			LocalDateTime.now()
		);
		
		e.printStackTrace();
				
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
	
	
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handlingException(
												NoResourceFoundException e,
												HttpServletRequest request
	){
		ApiError apiError = new ApiError(
			request.getRequestURI().toString(),
			HttpStatus.NOT_FOUND,
			e.getMessage(),
			LocalDateTime.now()
		);
				
		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}
	
	
	@ExceptionHandler(MailSendException.class)
	public ResponseEntity<ApiError> handlingException(
												MailSendException e,
												HttpServletRequest request
	){
		ApiError apiError = new ApiError(
			request.getRequestURI().toString(),
			HttpStatus.INTERNAL_SERVER_ERROR,
			"can't send emails now try resend token again later",
			LocalDateTime.now()
		);
				
		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}	
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> handlingException(
									HttpRequestMethodNotSupportedException e,
									HttpServletRequest request
	){
		ApiError apiError = new ApiError(
			request.getRequestURI().toString(),
			HttpStatus.BAD_REQUEST,
			e.getMessage(),
			LocalDateTime.now()
		);
				
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handlingGeneralException(
													Exception e,
													HttpServletRequest request
	){
		ApiError apiError = new ApiError(
				request.getRequestURI().toString(),
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal server Error",
				LocalDateTime.now()
		);
		
		e.printStackTrace();
		
		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
