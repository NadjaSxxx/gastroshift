package com.github.nadjasxxx.gastroshift.unavailability;

public class InvalidUnavailabilityTimeRangeException extends RuntimeException {
    public InvalidUnavailabilityTimeRangeException() {

        super("Unavailability end time must be after start time");
    }
}
