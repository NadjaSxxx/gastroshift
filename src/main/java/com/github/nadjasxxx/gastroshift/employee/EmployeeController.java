package com.github.nadjasxxx.gastroshift.employee;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
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

}
