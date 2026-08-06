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
}