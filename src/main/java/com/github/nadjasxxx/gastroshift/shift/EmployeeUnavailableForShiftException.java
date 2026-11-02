package com.github.nadjasxxx.gastroshift.shift;

import java.util.UUID;

public class EmployeeUnavailableForShiftException
        extends RuntimeException {

    public EmployeeUnavailableForShiftException(
            UUID employeeId
    ) {
        super(
                "Employee is unavailable during shift: "
                        + employeeId
        );
    }
}