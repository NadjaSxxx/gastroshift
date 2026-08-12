package com.github.nadjasxxx.gastroshift.employee;

import java.time.Instant;

public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
