package com.github.nadjasxxx.gastroshift.shift;

import com.github.nadjasxxx.gastroshift.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        shiftRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void shouldCreateShift() throws Exception {
        String requestBody = """
            {
              "startTime": "2026-08-15T17:00:00",
              "endTime": "2026-08-15T23:30:00",
              "position": "Service",
              "notes": "Terrace"
            }
            """;

        mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.startTime").value("2026-08-15T17:00:00"))
                .andExpect(jsonPath("$.endTime").value("2026-08-15T23:30:00"))
                .andExpect(jsonPath("$.position").value("Service"))
                .andExpect(jsonPath("$.notes").value("Terrace"));
    }

    @Test
    void shouldReturnAllShifts() throws Exception {
        shiftRepository.save(new Shift(
                java.util.UUID.fromString("33333333-3333-3333-3333-333333333333"),
                java.time.LocalDateTime.parse("2026-08-16T10:00:00"),
                java.time.LocalDateTime.parse("2026-08-16T16:00:00"),
                "Kitchen",
                null
        ));

        mockMvc.perform(get("/api/shifts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].position").value("Kitchen"))
                .andExpect(jsonPath("$[0].notes").doesNotExist());
    }

    @Test
    void shouldRejectShiftWhenEndIsNotAfterStart() throws Exception {
        String requestBody = """
            {
              "startTime": "2026-08-15T18:00:00",
              "endTime": "2026-08-15T17:00:00",
              "position": "Service"
            }
            """;

        mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("End time must be after start time"));
    }


    @Test
    void shouldAssignEmployeeToShift() throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID shiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        employeeRepository.save(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        shiftRepository.save(new Shift(
                shiftId,
                LocalDateTime.parse("2026-08-20T17:00:00"),
                LocalDateTime.parse("2026-08-20T23:00:00"),
                "Service",
                null
        ));

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        shiftId,
                        employeeId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shiftId.toString()))
                .andExpect(jsonPath("$.position").value("Service"))
                .andExpect(jsonPath("$.employee.id")
                        .value(employeeId.toString()))
                .andExpect(jsonPath("$.employee.firstName").value("Mira"))
                .andExpect(jsonPath("$.employee.lastName").value("Beispiel"))
                .andExpect(jsonPath("$.employee.email")
                        .value("mira.beispiel@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenAssigningUnknownShift() throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID unknownShiftId =
                UUID.fromString("99999999-9999-9999-9999-999999999999");

        employeeRepository.save(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        unknownShiftId,
                        employeeId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Shift not found: " + unknownShiftId));
    }

    @Test
    void shouldReturnNotFoundWhenAssigningUnknownEmployee() throws Exception {
        UUID shiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID unknownEmployeeId =
                UUID.fromString("99999999-9999-9999-9999-999999999999");

        shiftRepository.save(new Shift(
                shiftId,
                LocalDateTime.parse("2026-08-20T17:00:00"),
                LocalDateTime.parse("2026-08-20T23:00:00"),
                "Service",
                null
        ));

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        shiftId,
                        unknownEmployeeId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Employee not found: " + unknownEmployeeId));
    }
}