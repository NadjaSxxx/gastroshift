package com.github.nadjasxxx.gastroshift.employee;

import java.util.UUID;

public class InactiveEmployeeException extends RuntimeException{

    public InactiveEmployeeException(UUID employeeId) {
        super("Employee is inactive: " + employeeId);
    }
}
