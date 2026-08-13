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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShiftRepository shiftRepository;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
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
}