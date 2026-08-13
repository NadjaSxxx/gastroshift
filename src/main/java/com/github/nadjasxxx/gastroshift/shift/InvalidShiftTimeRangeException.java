package com.github.nadjasxxx.gastroshift.shift;

public class InvalidShiftTimeRangeException extends RuntimeException{

    public InvalidShiftTimeRangeException() {
        super("End time must be after start time");
    }
}
