package com.github.nadjasxxx.gastroshift.unavailability;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeUnavailabilityResponse(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String notes
) {
    public static EmployeeUnavailabilityResponse from(
            EmployeeUnavailability unavailability
    ) {
        return new EmployeeUnavailabilityResponse(
                unavailability.getId(),
                unavailability.getStartTime(),
                unavailability.getEndTime(),
                unavailability.getNotes()
        );
    }
}