package com.github.nadjasxxx.gastroshift.employee;

import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        boolean active
) {

    public static EmployeeResponse from (Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.isActive()
        );
    }
}
