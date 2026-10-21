package com.cricpulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cricPulseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CricPulse — Live Cricket Score Management System API")
                        .description("Comprehensive REST API and real-time Server-Sent Events (SSE) scoring platform for live cricket matches, player stats, and admin score management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CricPulse Engineering Team")
                                .email("support@cricpulse.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
