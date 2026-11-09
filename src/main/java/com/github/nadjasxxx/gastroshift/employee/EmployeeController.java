package com.github.nadjasxxx.gastroshift.employee;

import com.github.nadjasxxx.gastroshift.unavailability.EmployeeUnavailabilityService;
import com.github.nadjasxxx.gastroshift.unavailability.EmployeeUnavailabilityResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeUnavailabilityService unavailabilityService;

    public EmployeeController(EmployeeService employeeService,
                              EmployeeUnavailabilityService unavailabilityService) {

        this.employeeService = employeeService;
        this.unavailabilityService = unavailabilityService;
    }

    @GetMapping
    public List<EmployeeResponse> findAll() {

        return employeeService.findAll()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.create(request)
        );
    }

    @PatchMapping("/{employeeId}/status")
    public EmployeeResponse updateStatus(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeStatusRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.updateStatus(employeeId, request)
        );
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse update(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return EmployeeResponse.from(
                employeeService.update(employeeId, request)
        );
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponse findById(
            @PathVariable UUID employeeId
    ) {
        return EmployeeResponse.from(
                employeeService.findById(employeeId)
        );
    }

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

    @GetMapping("/{employeeId}/unavailability")
    public List<EmployeeUnavailabilityResponse>
    findEmployeeUnavailability(
            @PathVariable UUID employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,
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
