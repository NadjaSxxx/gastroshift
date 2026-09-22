package com.github.nadjasxxx.gastroshift.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter =
            new KeycloakRealmRoleConverter();

    @Test
    void shouldConvertKeycloakRealmRolesToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of(
                                "roles",
                                List.of("MANAGER", "EMPLOYEE")
                        )
                )
                .build();

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(
                new SimpleGrantedAuthority("ROLE_MANAGER")
        ));
        assertTrue(authorities.contains(
                new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        ));
    }

    @Test
    void shouldReturnNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .build();

        Collection<GrantedAuthority> authorities =
                converter.convert(jwt);

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }
}