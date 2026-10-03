package com.github.nadjasxxx.gastroshift.employee;

import jakarta.validation.constraints.NotBlank;

public record LinkEmployeeIdentityRequest(
        @NotBlank(message = "Identity subject must not be blank")
        String identitySubject
) {
}
