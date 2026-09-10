package com.github.nadjasxxx.gastroshift.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @NotBlank(message = "First name must not be blank")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email

) {
}
