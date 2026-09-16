package com.github.nadjasxxx.gastroshift.shift;

import com.github.nadjasxxx.gastroshift.employee.EmployeeResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShiftResponse(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String position,
        String notes,
        EmployeeResponse employee
) {

    public static ShiftResponse from(Shift shift) {

        EmployeeResponse employeeResponse =
                shift.getEmployee() == null
                ? null
                : EmployeeResponse.from(shift.getEmployee());

        return new ShiftResponse(
            shift.getId(),
            shift.getStartTime(),
            shift.getEndTime(),
            shift.getPosition(),
            shift.getNotes(),
            employeeResponse
        );

    }
}
