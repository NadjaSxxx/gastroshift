package com.github.nadjasxxx.gastroshift.employee;

import com.github.nadjasxxx.gastroshift.unavailability.EmployeeUnavailabilityService;
import com.github.nadjasxxx.gastroshift.unavailability.EmployeeUnavailabilityResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(
        name = "Employees",
        description = "Manage employees and their application identities"
)
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeUnavailabilityService unavailabilityService;

    public EmployeeController(EmployeeService employeeService,
                              EmployeeUnavailabilityService unavailabilityService) {

        this.employeeService = employeeService;
        this.unavailabilityService = unavailabilityService;
    }

    @Operation(
            summary = "List employees",
            description = """
                Returns all employees, including their current active status.
                Requires the MANAGER role.
                """
    )
    @GetMapping
    public List<EmployeeResponse> findAll() {

        return employeeService.findAll()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Operation(
            summary = "Create an employee",
            description = """
                Creates an active employee. Email addresses must be unique
                regardless of letter case. Requires the MANAGER role.
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.create(request)
        );
    }

    @Operation(
            summary = "Change an employee's active status",
            description = """
                Activates or deactivates an employee. Inactive employees
                cannot be assigned to new shifts. Existing assignments are
                preserved. Requires the MANAGER role.
                """
    )
    @PatchMapping("/{employeeId}/status")
    public EmployeeResponse updateStatus(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeStatusRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.updateStatus(employeeId, request)
        );
    }

    @Operation(
            summary = "Update an employee",
            description = """
                Updates the employee's name and email address while preserving
                the employee ID, active status and linked identity. Email
                addresses must remain unique. Requires the MANAGER role.
                """
    )
    @PutMapping("/{employeeId}")
    public EmployeeResponse update(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.update(employeeId, request)
        );
    }

    @Operation(
            summary = "Get an employee by ID",
            description = """
                Returns one employee identified by the database UUID.
                Requires the MANAGER role.
                """
    )
    @GetMapping("/{employeeId}")
    public EmployeeResponse findById(
            @PathVariable UUID employeeId
    ) {
        return EmployeeResponse.from(
                employeeService.findById(employeeId)
        );
    }

    @Operation(
            summary = "Link an employee identity",
            description = """
                Links an employee record to the stable subject identifier
                from a Keycloak access token. An identity subject can belong
                to only one employee. Requires the MANAGER role.
                """
    )
    @PutMapping("/{employeeId}/identity")
    public EmployeeResponse linkIdentity(
            @PathVariable UUID employeeId,
            @Valid @RequestBody LinkEmployeeIdentityRequest request
    ) {
        Employee employee = employeeService.linkIdentity(
                employeeId,
                request
        );

        return EmployeeResponse.from(employee);
    }

    @Operation(
            summary = "List an employee's unavailability periods",
            description = """
                Returns all unavailability periods for an employee or only
                periods overlapping the requested time range. The from and to
                parameters must either both be provided or both be omitted.
                Requires the MANAGER role.
                """
    )
    @GetMapping("/{employeeId}/unavailability")
    public List<EmployeeUnavailabilityResponse>
    findEmployeeUnavailability(
            @PathVariable UUID employeeId,
            @Parameter(
                    description = """
                        Start of the requested time range in ISO 8601 format.
                        Must be provided together with to.
                        """
            )
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,
            @Parameter(
                    description = """
                        End of the requested time range in ISO 8601 format.
                        Must be after from.
                        """
            )
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to
    ) {
        return unavailabilityService
                .findForEmployee(
                        employeeId,
                        from,
                        to
                )
                .stream()
                .map(EmployeeUnavailabilityResponse::from)
                .toList();
    }

}
