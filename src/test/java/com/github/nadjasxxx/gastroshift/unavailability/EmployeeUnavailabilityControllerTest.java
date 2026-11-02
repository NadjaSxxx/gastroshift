package com.github.nadjasxxx.gastroshift.unavailability;

import com.github.nadjasxxx.gastroshift.shift.Shift;
import com.github.nadjasxxx.gastroshift.TestcontainersConfiguration;
import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;
import com.github.nadjasxxx.gastroshift.shift.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EmployeeUnavailabilityControllerTest {

    private static final UUID ANNA_ID = UUID.fromString(
            "11111111-1111-1111-1111-111111111111"
    );

    private static final UUID RAINER_ID = UUID.fromString(
            "22222222-2222-2222-2222-222222222222"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private EmployeeUnavailabilityRepository
            unavailabilityRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Employee anna;
    private Employee rainer;

    @BeforeEach
    void setUp() {
        unavailabilityRepository.deleteAll();
        shiftRepository.deleteAll();
        employeeRepository.deleteAll();

        anna = new Employee(
                ANNA_ID,
                "Anna",
                "Nass",
                "anna.nass@example.com",
                true
        );
        anna.linkIdentity("keycloak-anna");

        rainer = new Employee(
                RAINER_ID,
                "Rainer",
                "Zufall",
                "rainer.zufall@example.com",
                true
        );
        rainer.linkIdentity("keycloak-rainer");

        employeeRepository.saveAllAndFlush(
                List.of(anna, rainer)
        );
    }

    @Test
    void shouldCreateOwnUnavailability() throws Exception {
        mockMvc.perform(post("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startTime": "2026-11-10T08:00:00",
                                  "endTime": "2026-11-10T16:00:00",
                                  "notes": "Medical appointment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value(
                        "2026-11-10T08:00:00"
                ))
                .andExpect(jsonPath("$.endTime").value(
                        "2026-11-10T16:00:00"
                ))
                .andExpect(jsonPath("$.notes").value(
                        "Medical appointment"
                ));

        assertTrue(
                unavailabilityRepository.existsOverlapping(
                        ANNA_ID,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T16:00:00"
                        )
                )
        );
    }

    @Test
    void shouldReturnOnlyOwnUnavailabilityInOrder()
            throws Exception {
        EmployeeUnavailability annaLater =
                new EmployeeUnavailability(
                        UUID.fromString(
                                "44444444-4444-4444-4444-444444444444"
                        ),
                        anna,
                        LocalDateTime.parse(
                                "2026-11-12T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-12T12:00:00"
                        ),
                        null
                );

        EmployeeUnavailability annaEarlier =
                new EmployeeUnavailability(
                        UUID.fromString(
                                "33333333-3333-3333-3333-333333333333"
                        ),
                        anna,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T12:00:00"
                        ),
                        null
                );

        EmployeeUnavailability rainerPeriod =
                new EmployeeUnavailability(
                        UUID.fromString(
                                "55555555-5555-5555-5555-555555555555"
                        ),
                        rainer,
                        LocalDateTime.parse(
                                "2026-11-09T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-09T12:00:00"
                        ),
                        null
                );

        unavailabilityRepository.saveAllAndFlush(
                List.of(
                        annaLater,
                        annaEarlier,
                        rainerPeriod
                )
        );

        mockMvc.perform(get("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna")))
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
    void shouldDeleteOwnUnavailability() throws Exception {
        UUID periodId = UUID.fromString(
                "33333333-3333-3333-3333-333333333333"
        );

        unavailabilityRepository.saveAndFlush(
                new EmployeeUnavailability(
                        periodId,
                        anna,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T12:00:00"
                        ),
                        null
                )
        );

        mockMvc.perform(delete(
                        "/api/me/unavailability/{unavailabilityId}",
                        periodId
                )
                        .with(employeeJwt("keycloak-anna")))
                .andExpect(status().isNoContent());

        assertFalse(
                unavailabilityRepository.existsById(periodId)
        );
    }

    @Test
    void shouldNotDeleteAnotherEmployeesUnavailability()
            throws Exception {
        UUID periodId = UUID.fromString(
                "55555555-5555-5555-5555-555555555555"
        );

        unavailabilityRepository.saveAndFlush(
                new EmployeeUnavailability(
                        periodId,
                        rainer,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T12:00:00"
                        ),
                        null
                )
        );

        mockMvc.perform(delete(
                        "/api/me/unavailability/{unavailabilityId}",
                        periodId
                )
                        .with(employeeJwt("keycloak-anna")))
                .andExpect(status().isNotFound());

        assertTrue(
                unavailabilityRepository.existsById(periodId)
        );
    }

    @Test
    void shouldRejectUnauthenticatedRequest()
            throws Exception {
        mockMvc.perform(get("/api/me/unavailability"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectManagerWithoutEmployeeRole()
            throws Exception {
        mockMvc.perform(get("/api/me/unavailability")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_MANAGER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor employeeJwt(
            String identitySubject
    ) {
        return jwt()
                .jwt(jwt -> jwt.subject(identitySubject))
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_EMPLOYEE"
                        )
                );
    }

    @Test
    void shouldRejectInvalidTimeRange() throws Exception {
        mockMvc.perform(post("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "startTime": "2026-11-10T16:00:00",
                              "endTime": "2026-11-10T08:00:00",
                              "notes": null
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Unavailability end time must be after start time"
                ));
    }

    @Test
    void shouldRejectOverlappingUnavailability()
            throws Exception {
        unavailabilityRepository.saveAndFlush(
                new EmployeeUnavailability(
                        UUID.randomUUID(),
                        anna,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T16:00:00"
                        ),
                        null
                )
        );

        mockMvc.perform(post("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "startTime": "2026-11-10T12:00:00",
                              "endTime": "2026-11-10T18:00:00"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Employee already has overlapping unavailability"
                ));
    }

    @Test
    void shouldRejectUnavailabilityOverlappingAssignedShift()
            throws Exception {
        Shift shift = new Shift(
                UUID.randomUUID(),
                LocalDateTime.parse("2026-11-10T10:00:00"),
                LocalDateTime.parse("2026-11-10T16:00:00"),
                "Service",
                null
        );
        shift.assignEmployee(anna);
        shiftRepository.saveAndFlush(shift);

        mockMvc.perform(post("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "startTime": "2026-11-10T08:00:00",
                              "endTime": "2026-11-10T12:00:00"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Unavailability overlaps an assigned shift"
                ));
    }

    @Test
    void shouldAllowUnavailabilityTouchingExistingBoundary()
            throws Exception {
        unavailabilityRepository.saveAndFlush(
                new EmployeeUnavailability(
                        UUID.randomUUID(),
                        anna,
                        LocalDateTime.parse(
                                "2026-11-10T08:00:00"
                        ),
                        LocalDateTime.parse(
                                "2026-11-10T12:00:00"
                        ),
                        null
                )
        );

        mockMvc.perform(post("/api/me/unavailability")
                        .with(employeeJwt("keycloak-anna"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "startTime": "2026-11-10T12:00:00",
                              "endTime": "2026-11-10T16:00:00"
                            }
                            """))
                .andExpect(status().isCreated());
    }
}