package com.github.nadjasxxx.gastroshift.shift;

import jakarta.validation.constraints.NotNull;

public record UpdateShiftStatusRequest(
        @NotNull(message = "Status must not be null")
        ShiftStatus status
) {
}