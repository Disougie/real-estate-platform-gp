package com.disougie.redis;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.disougie.exception.ApiError;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
	
	private final RateLimitService rateLimitService;
	private final ObjectMapper objectMapper;
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		
		if(!rateLimitService.isAllowed(request.getRemoteAddr())) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType("application/json");
			ApiError apiError = new ApiError(
					request.getContextPath(),
					HttpStatus.TOO_MANY_REQUESTS, 
					"you are exceeded your limit try again later", 
					LocalDateTime.now()
			);
			response.getWriter().write(objectMapper.writeValueAsString(apiError));
			return;
		}
		filterChain.doFilter(request, response);
	}

}
