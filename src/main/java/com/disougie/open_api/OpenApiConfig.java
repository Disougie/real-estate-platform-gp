package com.disougie.open_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
		info = @Info(
				contact = @Contact(
						name = "Ahmed Disougie",
						email = "adisougie@gmail.com"
				),
				title = "Property Platform API",
				description = "Api endpoints for property platform",
				version = "V1.0"
		),
		servers = {
				@Server(
						description = "local env (devolopment)",
						url = "http://localhost:8080"
						
				),
				@Server(
						description = "cloud env (production)",
						url = "https://propertyplatform.com"
				)
		},
		security = @SecurityRequirement(
				name = "BearerAuth"
		)
)
@SecurityScheme(
		name = "BearerAuth",
		description = "JWT Authentication",
		scheme = "bearer",
		bearerFormat = "JWT",
		type = SecuritySchemeType.HTTP,
		in = SecuritySchemeIn.HEADER
		
)
public class OpenApiConfig {

}
