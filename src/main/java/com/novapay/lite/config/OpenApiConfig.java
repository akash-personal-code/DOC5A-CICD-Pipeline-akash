package com.novapay.lite.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI novapayOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("NovaPay Lite API")
                        .description("Demo application for testing CI/CD pipelines")
                        .version("v0.0.1"));
    }
}
