package com.github.nadjasxxx.gastroshift.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import com.github.nadjasxxx.gastroshift.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        employeeRepository.saveAll(List.of(
                new Employee(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Anna",
                        "Nass",
                        "anna.nass@example.com",
                        true
                ),
                new Employee(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "Rainer",
                        "Zufall",
                        "rainer.zufall@example.com",
                        true
                )
        ));
    }

    @Test
    void shouldReturnAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Anna"))
                .andExpect(jsonPath("$[1].active").value(true));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        String requestBody = """
            {
              "firstName": "Mira",
              "lastName": "Beispiel",
              "email": "mira.beispiel@example.com"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Mira"))
                .andExpect(jsonPath("$.lastName").value("Beispiel"))
                .andExpect(jsonPath("$.email").value("mira.beispiel@example.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidEmployee() throws Exception {
        String requestBody = """
            {
              "firstName": "",
              "lastName": "Beispiel",
              "email": "keine-gueltige-email"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() throws Exception {
        String requestBody = """
            {
              "firstName": "Andere",
              "lastName": "Person",
              "email": "ANNA.NASS@EXAMPLE.COM"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("An employee with email 'ANNA.NASS@EXAMPLE.COM' already exists"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldDeactivateEmployee() throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");

        employeeRepository.saveAndFlush(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        mockMvc.perform(patch(
                        "/api/employees/{employeeId}/status",
                        employeeId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "active": false
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.active").value(false));

        Employee persistedEmployee = employeeRepository.findById(employeeId)
                .orElseThrow();

        assertFalse(persistedEmployee.isActive());
    }

    @Test
    void shouldReactivateEmployee() throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");

        employeeRepository.saveAndFlush(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                false
        ));

        mockMvc.perform(patch(
                        "/api/employees/{employeeId}/status",
                        employeeId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "active": true
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        Employee persistedEmployee = employeeRepository.findById(employeeId)
                .orElseThrow();

        assertTrue(persistedEmployee.isActive());
    }

    @Test
    void shouldRejectEmployeeStatusWhenActiveIsMissing()
            throws Exception {
        UUID employeeId =
                UUID.fromString("11111111-1111-1111-1111-111111111111");

        employeeRepository.saveAndFlush(new Employee(
                employeeId,
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com",
                true
        ));

        mockMvc.perform(patch(
                        "/api/employees/{employeeId}/status",
                        employeeId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownEmployee()
            throws Exception {
        UUID unknownEmployeeId =
                UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(patch(
                        "/api/employees/{employeeId}/status",
                        unknownEmployeeId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "active": false
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Employee not found: " + unknownEmployeeId));
    }

}