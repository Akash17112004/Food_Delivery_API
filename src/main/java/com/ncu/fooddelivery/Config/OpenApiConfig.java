package com.ncu.fooddelivery.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI foodDeliveryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Food Delivery API")
                        .description("API documentation for the Food Delivery System")
                        .version("1.0.0"));
    }
}

