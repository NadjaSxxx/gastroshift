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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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

    @Test
    void shouldRejectOverlappingAssignmentAndKeepExistingEmployee()
            throws Exception {
        UUID conflictingEmployeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID originalEmployeeId =
                UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID existingShiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID targetShiftId =
                UUID.fromString("44444444-4444-4444-4444-444444444444");

        Employee conflictingEmployee = employeeRepository.save(new Employee(
                conflictingEmployeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        Employee originalEmployee = employeeRepository.save(new Employee(
                originalEmployeeId,
                "Nora",
                "Muster",
                "nora.muster@example.com",
                true
        ));

        Shift existingShift = new Shift(
                existingShiftId,
                LocalDateTime.parse("2026-08-20T10:00:00"),
                LocalDateTime.parse("2026-08-20T16:00:00"),
                "Kitchen",
                null
        );
        existingShift.assignEmployee(conflictingEmployee);
        shiftRepository.save(existingShift);

        Shift targetShift = new Shift(
                targetShiftId,
                LocalDateTime.parse("2026-08-20T15:00:00"),
                LocalDateTime.parse("2026-08-20T18:00:00"),
                "Service",
                null
        );
        targetShift.assignEmployee(originalEmployee);
        shiftRepository.saveAndFlush(targetShift);

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        targetShiftId,
                        conflictingEmployeeId
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Employee already has an overlapping shift: "
                                + conflictingEmployeeId
                ));

        Shift unchangedShift = shiftRepository.findById(targetShiftId)
                .orElseThrow();

        assertEquals(
                originalEmployeeId,
                unchangedShift.getEmployee().getId()
        );
    }

    @Test
    void shouldAllowAssignmentWhenShiftsOnlyTouchAtBoundary()
            throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID existingShiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID targetShiftId =
                UUID.fromString("44444444-4444-4444-4444-444444444444");

        Employee employee = employeeRepository.save(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        Shift existingShift = new Shift(
                existingShiftId,
                LocalDateTime.parse("2026-08-20T10:00:00"),
                LocalDateTime.parse("2026-08-20T16:00:00"),
                "Kitchen",
                null
        );
        existingShift.assignEmployee(employee);
        shiftRepository.save(existingShift);

        shiftRepository.saveAndFlush(new Shift(
                targetShiftId,
                LocalDateTime.parse("2026-08-20T16:00:00"),
                LocalDateTime.parse("2026-08-20T20:00:00"),
                "Service",
                null
        ));

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        targetShiftId,
                        employeeId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetShiftId.toString()))
                .andExpect(jsonPath("$.employee.id")
                        .value(employeeId.toString()));
    }

    @Test
    void shouldAllowAssigningSameEmployeeToSameShiftAgain()
            throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID shiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        Employee employee = employeeRepository.save(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        Shift shift = new Shift(
                shiftId,
                LocalDateTime.parse("2026-08-20T10:00:00"),
                LocalDateTime.parse("2026-08-20T16:00:00"),
                "Kitchen",
                null
        );
        shift.assignEmployee(employee);
        shiftRepository.saveAndFlush(shift);

        mockMvc.perform(put(
                        "/api/shifts/{shiftId}/employee/{employeeId}",
                        shiftId,
                        employeeId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shiftId.toString()))
                .andExpect(jsonPath("$.employee.id")
                        .value(employeeId.toString()));
    }

    @Test
    void shouldUnassignEmployeeFromShift() throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID shiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        Employee employee = employeeRepository.save(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        Shift shift = new Shift(
                shiftId,
                LocalDateTime.parse("2026-08-20T10:00:00"),
                LocalDateTime.parse("2026-08-20T16:00:00"),
                "Kitchen",
                null
        );
        shift.assignEmployee(employee);
        shiftRepository.saveAndFlush(shift);

        mockMvc.perform(delete(
                        "/api/shifts/{shiftId}/employee",
                        shiftId
                ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Shift persistedShift = shiftRepository.findById(shiftId)
                .orElseThrow();

        assertNull(persistedShift.getEmployee());
    }

    @Test
    void shouldAllowUnassigningAlreadyUnassignedShift() throws Exception {
        UUID shiftId =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        shiftRepository.saveAndFlush(new Shift(
                shiftId,
                LocalDateTime.parse("2026-08-20T10:00:00"),
                LocalDateTime.parse("2026-08-20T16:00:00"),
                "Kitchen",
                null
        ));

        mockMvc.perform(delete(
                        "/api/shifts/{shiftId}/employee",
                        shiftId
                ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Shift persistedShift = shiftRepository.findById(shiftId)
                .orElseThrow();

        assertNull(persistedShift.getEmployee());
    }

    @Test
    void shouldReturnNotFoundWhenUnassigningUnknownShift()
            throws Exception {
        UUID unknownShiftId =
                UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(delete(
                        "/api/shifts/{shiftId}/employee",
                        unknownShiftId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Shift not found: " + unknownShiftId));
    }

    @Test
    void shouldReturnShiftsOverlappingRequestedRange() throws Exception {
        shiftRepository.saveAll(List.of(
                new Shift(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        LocalDateTime.parse("2026-09-07T08:00:00"),
                        LocalDateTime.parse("2026-09-07T10:00:00"),
                        "Ends at boundary",
                        null
                ),
                new Shift(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        LocalDateTime.parse("2026-09-07T09:00:00"),
                        LocalDateTime.parse("2026-09-07T11:00:00"),
                        "Overlaps start",
                        null
                ),
                new Shift(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        LocalDateTime.parse("2026-09-07T12:00:00"),
                        LocalDateTime.parse("2026-09-07T16:00:00"),
                        "Inside range",
                        null
                ),
                new Shift(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        LocalDateTime.parse("2026-09-07T17:00:00"),
                        LocalDateTime.parse("2026-09-07T19:00:00"),
                        "Overlaps end",
                        null
                ),
                new Shift(
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        LocalDateTime.parse("2026-09-07T18:00:00"),
                        LocalDateTime.parse("2026-09-07T20:00:00"),
                        "Starts at boundary",
                        null
                )
        ));
        shiftRepository.flush();

        mockMvc.perform(get("/api/shifts")
                        .param("from", "2026-09-07T10:00:00")
                        .param("to", "2026-09-07T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].position")
                        .value("Overlaps start"))
                .andExpect(jsonPath("$[1].position")
                        .value("Inside range"))
                .andExpect(jsonPath("$[2].position")
                        .value("Overlaps end"));
    }

    @Test
    void shouldRejectShiftFilterWhenToIsMissing() throws Exception {
        mockMvc.perform(get("/api/shifts")
                        .param("from", "2026-09-07T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Both from and to must be provided"));
    }

    @Test
    void shouldRejectShiftFilterWhenToIsNotAfterFrom()
            throws Exception {
        mockMvc.perform(get("/api/shifts")
                        .param("from", "2026-09-07T18:00:00")
                        .param("to", "2026-09-07T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("To must be after from"));
    }

    @Test
    void shouldRejectShiftFilterWithInvalidDateFormat()
            throws Exception {
        mockMvc.perform(get("/api/shifts")
                        .param("from", "not-a-date")
                        .param("to", "2026-09-07T18:00:00"))
                .andExpect(status().isBadRequest());
    }
}