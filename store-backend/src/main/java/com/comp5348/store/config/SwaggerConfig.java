package com.comp5348.store.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SwaggerConfig {

    @Value("${spring.openapi.dev-url}")
    private String devUrl;

    @Bean
    public OpenAPI customerOpenAPI() {
        Server devServer = new Server();

        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Info info = new Info()
            .title("store-backend")
            .version("1.0")
            .description("API Description.");
        return new OpenAPI().info(info).servers(List.of(devServer));
    }
}
