package com.github.nadjasxxx.gastroshift.employee;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final List<Employee> employees = List.of(
            new Employee(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "Anna",
                    "Nass",
                    "Anna.Nass@example.com",
                    true),
            new Employee(
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    "Rainer",
                    "Zufall",
                    "Rainer.Zufall@example.com",
                    true)
    );

    public List<Employee> findAll() {
        return employees;
    }


}
