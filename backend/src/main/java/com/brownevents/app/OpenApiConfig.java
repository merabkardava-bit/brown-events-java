package com.brownevents.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Brown Events API",
                version = "1.0.0",
                description = "REST API for the Brown Events conference management platform. " +
                        "Manage conferences, sessions, speakers, attendees, and registrations.",
                contact = @Contact(
                        name = "Brown Events Team",
                        email = "support@brownevents.com"
                )
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local development server")
)
@Configuration
public class OpenApiConfig {
    // springdoc-openapi picks up this class automatically via @OpenAPIDefinition;
    // no bean definition is required for basic setup.
}
