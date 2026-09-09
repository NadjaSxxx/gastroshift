package com.github.nadjasxxx.gastroshift.employee;

import jakarta.validation.constraints.NotNull;

public record UpdateEmployeeStatusRequest(
        @NotNull(message = "Active status must not be null")
        Boolean active
) {
}
