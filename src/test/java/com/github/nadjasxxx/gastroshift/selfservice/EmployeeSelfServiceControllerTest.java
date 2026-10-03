package com.github.nadjasxxx.gastroshift.selfservice;

import com.github.nadjasxxx.gastroshift.TestcontainersConfiguration;
import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;
import com.github.nadjasxxx.gastroshift.shift.Shift;
import com.github.nadjasxxx.gastroshift.shift.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EmployeeSelfServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        employeeRepository.deleteAll();

        Employee anna = new Employee(
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"
                ),
                "Anna",
                "Nass",
                "anna.nass@example.com",
                true
        );
        anna.linkIdentity("keycloak-anna");

        Employee rainer = new Employee(
                UUID.fromString(
                        "22222222-2222-2222-2222-222222222222"
                ),
                "Rainer",
                "Zufall",
                "rainer.zufall@example.com",
                true
        );
        rainer.linkIdentity("keycloak-rainer");

        employeeRepository.saveAllAndFlush(
                List.of(anna, rainer)
        );

        Shift annaLaterShift = new Shift(
                UUID.fromString(
                        "44444444-4444-4444-4444-444444444444"
                ),
                LocalDateTime.parse("2026-10-05T16:00:00"),
                LocalDateTime.parse("2026-10-05T22:00:00"),
                "Service",
                null
        );
        annaLaterShift.assignEmployee(anna);

        Shift annaEarlierShift = new Shift(
                UUID.fromString(
                        "33333333-3333-3333-3333-333333333333"
                ),
                LocalDateTime.parse("2026-10-04T10:00:00"),
                LocalDateTime.parse("2026-10-04T16:00:00"),
                "Kitchen",
                null
        );
        annaEarlierShift.assignEmployee(anna);

        Shift rainerShift = new Shift(
                UUID.fromString(
                        "55555555-5555-5555-5555-555555555555"
                ),
                LocalDateTime.parse("2026-10-04T12:00:00"),
                LocalDateTime.parse("2026-10-04T18:00:00"),
                "Bar",
                null
        );
        rainerShift.assignEmployee(rainer);

        shiftRepository.saveAllAndFlush(
                List.of(
                        annaLaterShift,
                        annaEarlierShift,
                        rainerShift
                )
        );
    }

    @Test
    void shouldReturnOnlyOwnShiftsInChronologicalOrder()
            throws Exception {
        mockMvc.perform(get("/api/me/shifts")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("keycloak-anna")
                                )
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_EMPLOYEE"
                                        )
                                )
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(
                        "33333333-3333-3333-3333-333333333333"
                ))
                .andExpect(jsonPath("$[1].id").value(
                        "44444444-4444-4444-4444-444444444444"
                ));
    }

    @Test
    void shouldReturnNotFoundWhenIdentityIsNotLinked()
            throws Exception {
        mockMvc.perform(get("/api/me/shifts")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("unknown-user")
                                )
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_EMPLOYEE"
                                        )
                                )
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "No employee is linked to the authenticated identity"
                ));
    }

    @Test
    void shouldRejectManagerWithoutEmployeeRole()
            throws Exception {
        mockMvc.perform(get("/api/me/shifts")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_MANAGER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedRequest()
            throws Exception {
        mockMvc.perform(get("/api/me/shifts"))
                .andExpect(status().isUnauthorized());
    }
}