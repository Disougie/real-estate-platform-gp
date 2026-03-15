package com.disougie.security;

import static com.disougie.app_user.AppUserRole.ADMIN;
import static com.disougie.app_user.AppUserRole.LAWYER;
import static com.disougie.app_user.AppUserRole.USER;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	
	private final UserDetailsService userDetailsService;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JwtService jwtService;

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
		dao.setPasswordEncoder(passwordEncoder());
		return dao;
	}

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:8000"));
        config.setAllowedMethods(List.of("GET","POST","PUT", "PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(
						csrf -> csrf.disable()
				)
				.sessionManagement(
						session -> session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)
				.authorizeHttpRequests(
						auth -> auth
							.requestMatchers(
									"/",
									"/swagger-ui/**",
									"/v3/api-docs/**",
									"/swagger-ui.html",
									"/swagger-ui/index.html",
									
									"/ws/**",          
					                "/ws/info/**",     
					                "/topic/**",
					                "/queue/**"
									
							).permitAll()
							.requestMatchers(
									"/api/v*/registration",
									"/api/v*/login",
									"/api/v*/token/**",
									"/api/v1/forgot-password/**",
									"/api/v1/reset-password/**",
									"/reset/**",
									"/reset.html",
									"/images/**",
									"/keep-alive/**"
							).permitAll()
							.requestMatchers(
									"/api/v*/users/**",
									"/api/v*/properties/**",
									"/api/v*/initial-contracts/**",
									"/api/v*/blogs/**",
									"/api/v*/recommendation/**",
									"/api/v*/change/**"
							).hasRole(USER.getRole())
							.requestMatchers(
									"/api/v*/lawyer/**",
									"/api/v*/change/**"
							).hasRole(LAWYER.getRole())
							.requestMatchers(
									"/api/v*/admin/**",
									"/api/v*/change/**"
							).hasRole(ADMIN.getRole())
							.anyRequest().authenticated()
				)
				.addFilterBefore(
						new JwtTokenValidation(jwtService, userDetailsService),
						UsernamePasswordAuthenticationFilter.class
				)
				.build();
	}
}
