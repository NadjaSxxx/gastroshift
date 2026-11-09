package com.github.nadjasxxx.gastroshift.unavailability;

import com.github.nadjasxxx.gastroshift.employee.Employee;
import com.github.nadjasxxx.gastroshift.employee.EmployeeNotFoundException;
import com.github.nadjasxxx.gastroshift.employee.EmployeeRepository;
import com.github.nadjasxxx.gastroshift.selfservice.EmployeeIdentityNotLinkedException;
import com.github.nadjasxxx.gastroshift.shift.ShiftRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeUnavailabilityService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository
            unavailabilityRepository;
    private final ShiftRepository shiftRepository;

    public EmployeeUnavailabilityService(
            EmployeeRepository employeeRepository,
            EmployeeUnavailabilityRepository
                    unavailabilityRepository,
            ShiftRepository shiftRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.unavailabilityRepository =
                unavailabilityRepository;
        this.shiftRepository = shiftRepository;
    }

    public List<EmployeeUnavailability> findOwn(
            String identitySubject
    ) {
        Employee employee = findEmployee(identitySubject);

        return unavailabilityRepository
                .findByEmployee_IdOrderByStartTimeAsc(
                        employee.getId()
                );
    }

    public EmployeeUnavailability createOwn(
            String identitySubject,
            CreateUnavailabilityRequest request
    ) {
        if (!request.endTime().isAfter(
                request.startTime()
        )) {
            throw new InvalidUnavailabilityTimeRangeException();
        }

        Employee employee = findEmployee(identitySubject);

        boolean overlapsUnavailability =
                unavailabilityRepository.existsOverlapping(
                        employee.getId(),
                        request.startTime(),
                        request.endTime()
                );

        if (overlapsUnavailability) {
            throw new OverlappingUnavailabilityException();
        }

        boolean overlapsAssignedShift =
                shiftRepository
                        .existsAssignedShiftOverlapping(
                                employee.getId(),
                                request.startTime(),
                                request.endTime()
                        );

        if (overlapsAssignedShift) {
            throw new UnavailabilityOverlapsAssignedShiftException();
        }

        EmployeeUnavailability unavailability =
                new EmployeeUnavailability(
                        UUID.randomUUID(),
                        employee,
                        request.startTime(),
                        request.endTime(),
                        request.notes()
                );

        return unavailabilityRepository.saveAndFlush(
                unavailability
        );
    }

    public void deleteOwn(
            String identitySubject,
            UUID unavailabilityId
    ) {
        Employee employee = findEmployee(identitySubject);

        EmployeeUnavailability unavailability =
                unavailabilityRepository
                        .findByIdAndEmployee_Id(
                                unavailabilityId,
                                employee.getId()
                        )
                        .orElseThrow(() ->
                                new EmployeeUnavailabilityNotFoundException(
                                        unavailabilityId
                                )
                        );

        unavailabilityRepository.delete(unavailability);
        unavailabilityRepository.flush();
    }

    private Employee findEmployee(String identitySubject) {
        return employeeRepository
                .findByIdentitySubject(identitySubject)
                .orElseThrow(
                        EmployeeIdentityNotLinkedException::new
                );
    }

    public List<EmployeeUnavailability> findForEmployee(
            UUID employeeId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null || to == null) {
            throw new InvalidUnavailabilityFilterRangeException(
                    "Both from and to must be provided"
            );
        }

        if (!to.isAfter(from)) {
            throw new InvalidUnavailabilityFilterRangeException(
                    "To must be after from"
            );
        }

        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }

        return unavailabilityRepository
                .findOverlappingRangeForEmployee(
                        employeeId,
                        from,
                        to
                );
    }
}