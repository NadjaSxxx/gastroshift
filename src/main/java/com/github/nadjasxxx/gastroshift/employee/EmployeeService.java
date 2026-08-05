package com.github.nadjasxxx.gastroshift.employee;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final List<Employee> employees = new ArrayList<>(List.of(
            new Employee(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "Anna",
                    "Nass",
                    "anna.nass@example.com",
                    true),
            new Employee(
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    "Rainer",
                    "Zufall",
                    "rainer.zufall@example.com",
                    true)
    ));

    public List<Employee> findAll() {
        return List.copyOf(employees);
    }

    public Employee create(CreateEmployeeRequest request) {
        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                true
        );
        employees.add(employee);
        return employee;
    }

}
