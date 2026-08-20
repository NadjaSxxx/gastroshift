package com.github.nadjasxxx.gastroshift.shift;

import java.util.UUID;

public class OverlappingShiftException extends RuntimeException{

    public OverlappingShiftException(UUID employeeId) {
        super("Employee already has an overlapping shift: " + employeeId);
    }
}
