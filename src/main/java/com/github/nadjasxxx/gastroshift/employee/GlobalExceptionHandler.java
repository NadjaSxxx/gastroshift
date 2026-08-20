package com.github.nadjasxxx.gastroshift.employee;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import com.github.nadjasxxx.gastroshift.shift.InvalidShiftTimeRangeException;
import com.github.nadjasxxx.gastroshift.employee.EmployeeNotFoundException;
import com.github.nadjasxxx.gastroshift.shift.ShiftNotFoundException;
import com.github.nadjasxxx.gastroshift.shift.OverlappingShiftException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmployeeEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateEmployeeEmail(
            DuplicateEmployeeEmailException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The request conflicts with existing data",
                Instant.now()
        );
    }

    @ExceptionHandler(InvalidShiftTimeRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidShiftTimeRange(
            InvalidShiftTimeRangeException exception
    ) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler({
            EmployeeNotFoundException.class,
            ShiftNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(RuntimeException exception) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                Instant.now()
        );
    }

    @ExceptionHandler(OverlappingShiftException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOverlappingShift(
            OverlappingShiftException exception
    ) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                Instant.now()
        );
    }
}
