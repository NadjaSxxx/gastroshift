package com.github.nadjasxxx.gastroshift.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "keycloak";

    private final String issuerUri;

    public OpenApiConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuerUri
    ) {
        this.issuerUri = issuerUri;
    }

    @Bean
    public OpenAPI gastroShiftOpenApi() {
        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(
                        issuerUri + "/protocol/openid-connect/auth"
                )
                .tokenUrl(
                        issuerUri + "/protocol/openid-connect/token"
                )
                .scopes(new Scopes()
                        .addString(
                                "openid",
                                "Authenticate with Keycloak"
                        )
                );

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description(
                        "Authenticate through the GastroShift Keycloak realm"
                )
                .flows(new OAuthFlows()
                        .authorizationCode(authorizationCodeFlow)
                );

        return new OpenAPI()
                .info(new Info()
                        .title("GastroShift API")
                        .version("0.0.1")
                        .description(
                                "REST API for managing employees, shifts "
                                        + "and employee unavailability"
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                securityScheme
                        )
                );
    }
}