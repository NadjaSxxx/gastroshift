package com.github.nadjasxxx.gastroshift.unavailability;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateUnavailabilityRequest(
        @NotNull(message = "Start time must not be null")
        LocalDateTime startTime,

        @NotNull(message = "End time must not be null")
        LocalDateTime endTime,

        @Size(
                max = 500,
                message = "Notes must not exceed 500 characters"
        )
        String notes
) {
}