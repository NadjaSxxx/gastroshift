package com.github.nadjasxxx.gastroshift.shift;

import org.springframework.stereotype.Service;
import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeNotFoundException;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    private final EmployeeRepository employeeRepository;

    public ShiftService(
            ShiftRepository shiftRepository,
            EmployeeRepository employeeRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Shift> findAll() {
        return shiftRepository.findAll();
    }

    public Shift create(CreateShiftRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidShiftTimeRangeException();
        }
        Shift shift = new Shift(
                UUID.randomUUID(),
                request.startTime(),
                request.endTime(),
                request.position(),
                request.notes()
        );
        return shiftRepository.saveAndFlush(shift);
    }

    public List<Shift> findAll(
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null || to == null) {
            throw new InvalidShiftFilterRangeException(
                    "Both from and to must be provided"
            );
        }

        if (!to.isAfter(from)) {
            throw new InvalidShiftFilterRangeException(
                    "To must be after from"
            );
        }

        return shiftRepository.findOverlappingRange(from, to);
    }

    public Shift assignEmployee(UUID shiftId, UUID employeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        boolean hasOverlap = shiftRepository.existsOverlappingShift(
                employeeId,
                shiftId,
                shift.getStartTime(),
                shift.getEndTime()
        );

        if (hasOverlap) {
            throw new OverlappingShiftException(employeeId);
        }

        shift.assignEmployee(employee);

        return shiftRepository.saveAndFlush(shift);
    }

    public void unassignEmployee(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));

        shift.unassignEmployee();

        shiftRepository.saveAndFlush(shift);
    }
}
