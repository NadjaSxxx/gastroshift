package com.github.nadjasxxx.gastroshift.unavailability;

import java.util.UUID;

public class EmployeeUnavailabilityNotFoundException
        extends RuntimeException {

    public EmployeeUnavailabilityNotFoundException(
            UUID unavailabilityId
    ) {
        super(
                "Employee unavailability not found: "
                        + unavailabilityId
        );
    }
}