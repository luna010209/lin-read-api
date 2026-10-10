package io.lin.read.config.swaggerUI;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag().name("01. Profile").description("Current user profile from lin-auth-api"));
        tags.add(new Tag().name("09. Gemini").description("AI generation (text, image, audio)"));
        tags.add(new Tag().name("Root").description("Service info and health"));

        return new OpenAPI()
                .info(new Info()
                        .title("Lin Read API")
                        .version("1.0")
                        .description("LinRead API documentation"))
                .addSecurityItem(new SecurityRequirement().addList("JWT Token"))
                .components(new Components()
                        .addSecuritySchemes("JWT Token", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                )
                .tags(tags)
                .servers(List.of(
                        new Server().url("http://127.0.0.1:8082").description("Local")
                ));
    }

    private boolean shouldAddLanguageHeader(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().isAnnotationPresent(RestController.class);
    }

    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            if (shouldAddLanguageHeader(handlerMethod)) {
                Schema<String> languageSchema = new StringSchema()
                        ._enum(List.of("en", "ko", "vi", "ja", "zh"))
                        ._default("en");

                operation.addParametersItem(
                        new Parameter()
                                .in("header")
                                .name("Accept-Language")
                                .description("Select Language")
                                .required(false)
                                .schema(languageSchema)
                );
            }
            return operation;
        };
    }
}
