package com.github.nadjasxxx.gastroshift.selfservice;

import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;
import com.github.nadjasxxx.gastroshift.shift.Shift;
import com.github.nadjasxxx.gastroshift.shift.ShiftRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeSelfService {

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    public EmployeeSelfService(
            EmployeeRepository employeeRepository,
            ShiftRepository shiftRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> findOwnShifts(String identitySubject) {
        Employee employee = employeeRepository
                .findByIdentitySubject(identitySubject)
                .orElseThrow(
                        EmployeeIdentityNotLinkedException::new
                );

        return shiftRepository
                .findByEmployee_IdOrderByStartTimeAsc(
                        employee.getId()
                );
    }
}