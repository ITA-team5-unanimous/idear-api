package com.idear.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Auth";

    public SwaggerConfig(MappingJackson2HttpMessageConverter converter) {
        var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(setServers())
                .components(securitySchemes())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("iDear API")
                .description("Public API Server for iDear")
                .version("v1.0.0");
    }

    private List<Server> setServers() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080/api");
        localServer.setDescription("로컬 서버");

        Server prodServer = new Server();
        prodServer.setUrl("https://i-dear.info/api");
        prodServer.setDescription("배포 서버");

        return List.of(prodServer, localServer);
    }

    private Components securitySchemes() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")
                );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }
}
