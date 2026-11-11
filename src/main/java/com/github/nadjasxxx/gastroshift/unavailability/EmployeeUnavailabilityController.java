package com.github.nadjasxxx.gastroshift.unavailability;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/unavailability")
@Tag(
        name = "Employee Unavailability",
        description = "Manage employee unavailability periods"
)
public class EmployeeUnavailabilityController {

    private final EmployeeUnavailabilityService
            unavailabilityService;

    public EmployeeUnavailabilityController(
            EmployeeUnavailabilityService
                    unavailabilityService
    ) {
        this.unavailabilityService = unavailabilityService;
    }

    @Operation(
            summary = "List my unavailability periods",
            description = """
                Returns all unavailability periods belonging to the
                authenticated employee. The employee is identified through
                the subject claim of the access token. Requires the EMPLOYEE
                role and a linked employee identity.
                """
    )
    @GetMapping
    public List<EmployeeUnavailabilityResponse> findOwn(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return unavailabilityService
                .findOwn(jwt.getSubject())
                .stream()
                .map(EmployeeUnavailabilityResponse::from)
                .toList();
    }

    @Operation(
            summary = "Create my unavailability period",
            description = """
                Creates an unavailability period for the authenticated
                employee. The end time must be after the start time. The
                period must not overlap another unavailability period or an
                already assigned shift. Requires the EMPLOYEE role and a
                linked employee identity.
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeUnavailabilityResponse createOwn(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateUnavailabilityRequest request
    ) {
        EmployeeUnavailability created =
                unavailabilityService.createOwn(
                        jwt.getSubject(),
                        request
                );

        return EmployeeUnavailabilityResponse.from(created);
    }

    @Operation(
            summary = "Delete my unavailability period",
            description = """
                Deletes an unavailability period belonging to the
                authenticated employee. An employee cannot delete another
                employee's unavailability period. Requires the EMPLOYEE role
                and a linked employee identity.
                """
    )
    @DeleteMapping("/{unavailabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwn(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID unavailabilityId
    ) {
        unavailabilityService.deleteOwn(
                jwt.getSubject(),
                unavailabilityId
        );
    }
}