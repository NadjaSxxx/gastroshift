package com.github.nadjasxxx.gastroshift.security;

import com.github.nadjasxxx.gastroshift.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAuthenticatedRequestWithoutManagerRole()
            throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowRequestWithManagerRole() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_MANAGER"
                                )
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToOpenApiDocumentationWithoutAuthentication()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToSwaggerUiWithoutAuthentication()
            throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeConfiguredOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title")
                        .value("GastroShift API"))
                .andExpect(jsonPath("$.info.version")
                        .value("0.0.1"))
                .andExpect(jsonPath(
                        "$.components.securitySchemes.keycloak.type"
                ).value("oauth2"))
                .andExpect(jsonPath(
                        "$.components.securitySchemes.keycloak"
                                + ".flows.authorizationCode"
                ).exists())
                .andExpect(jsonPath(
                        "$['paths']['/api/shifts']"
                ).exists())
                .andExpect(jsonPath(
                        "$['paths']['/api/me/shifts']"
                ).exists());
    }
}
