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
        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                true
        );
        return employeeRepository.save(employee);
    }
}
