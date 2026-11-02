package com.github.nadjasxxx.gastroshift.unavailability;

public class OverlappingUnavailabilityException extends RuntimeException {
    public OverlappingUnavailabilityException() {
        super("Employee already has overlapping unavailability");
    }
}
