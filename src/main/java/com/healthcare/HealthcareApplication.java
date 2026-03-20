package com.healthcare;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@OpenAPIDefinition(
    info = @Info(
        title = "Healthcare Management System API",
        version = "1.0.0",
        description = "HIPAA-compliant Healthcare Management System with comprehensive patient, appointment, and medical record management",
        contact = @Contact(
            name = "Healthcare System Support",
            email = "support@healthcare.com",
            url = "https://healthcare.com"
        ),
        license = @License(
            name = "Proprietary",
            url = "https://healthcare.com/license"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server"),
        @Server(url = "https://api.healthcare.com", description = "Production Server")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
public class HealthcareApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthcareApplication.class, args);
    }
}