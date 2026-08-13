package com.github.nadjasxxx.gastroshift.shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateShiftRequest(
        @NotNull(message = "Start time must not be null")
        LocalDateTime startTime,

        @NotNull(message = "End time must not be null")
        LocalDateTime endTime,

        @NotBlank(message = "Position must not be blank")
        @Size(max = 100, message = "Position must not exceed 100 characters")
        String position,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}