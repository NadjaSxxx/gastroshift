package com.github.nadjasxxx.gastroshift.unavailability;

public class UnavailabilityOverlapsAssignedShiftException extends RuntimeException {
    public UnavailabilityOverlapsAssignedShiftException() {

        super("Unavailability overlaps an assigned shift");
    }
}
