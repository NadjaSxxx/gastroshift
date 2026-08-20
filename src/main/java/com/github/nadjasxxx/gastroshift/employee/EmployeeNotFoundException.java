package com.github.nadjasxxx.gastroshift.employee;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID id) {
        super("Employee not found: " + id);
    }
}
