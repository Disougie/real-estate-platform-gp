package com.disougie.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.disougie.security.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
	
	private final JwtService jwtService;
	
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler handler,
                                   Map<String, Object> attributes) {

        String token = ((ServletServerHttpRequest) request)
                .getServletRequest()
                .getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String username = jwtService.extractUsername(jwt);
            attributes.put("user", username);
        }
        return true;
    }

	@Override
	public void afterHandshake(ServerHttpRequest request,
								ServerHttpResponse response, 
								WebSocketHandler wsHandler,
								Exception exception) {

		System.out.println("Handshake completed succssfully");
		
	}

}

