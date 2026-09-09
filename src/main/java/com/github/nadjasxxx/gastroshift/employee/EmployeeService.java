package com.github.nadjasxxx.gastroshift.employee;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee create(CreateEmployeeRequest request) {

        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmployeeEmailException(request.email());
        }

        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                true
        );
        return employeeRepository.saveAndFlush(employee);
    }

    public Employee updateStatus(
            UUID employeeId,
            UpdateEmployeeStatusRequest request
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(employeeId)
                );

        employee.updateActiveStatus(request.active());

        return employeeRepository.saveAndFlush(employee);
    }
}
