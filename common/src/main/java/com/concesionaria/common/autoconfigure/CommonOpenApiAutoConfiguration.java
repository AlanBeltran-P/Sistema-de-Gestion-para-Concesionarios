package com.concesionaria.common.autoconfigure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Documentación OpenAPI de cada servicio (Swagger UI en /swagger-ui.html).
 * - Título y descripción salen de openapi.titulo / openapi.descripcion (config-repo).
 * - Botón "Authorize": se pega el accessToken del login y se manda como Bearer.
 * - Servidor "/": las pruebas desde Swagger van al mismo host desde el que se abrió
 *   (el gateway en http://localhost:8080/swagger-ui.html).
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springdoc.core.configuration.SpringDocConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonOpenApiAutoConfiguration {

    private static final String JWT = "bearerAuth";

    @Bean
    @ConditionalOnMissingBean
    OpenAPI openApi(@Value("${openapi.titulo:${spring.application.name}}") String titulo,
                    @Value("${openapi.descripcion:}") String descripcion) {
        return new OpenAPI()
                .info(new Info()
                        .title(titulo)
                        .version("1.0")
                        .description(descripcion
                                + "\n\nTodas las rutas pasan por el api-gateway (http://localhost:8080). "
                                + "Para las que requieren login: POST /api/auth/login, copiar el accessToken "
                                + "y pegarlo en **Authorize**."))
                .servers(List.of(new Server().url("/").description("api-gateway")))
                .components(new Components().addSecuritySchemes(JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("accessToken que devuelve POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(JWT));
    }
}
