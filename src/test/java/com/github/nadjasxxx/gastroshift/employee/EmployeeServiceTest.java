package com.github.nadjasxxx.gastroshift.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldPropagateDatabaseConflictDuringCreation() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Mira",
                "Beispiel",
                "mira.beispiel@example.com"
        );

        when(employeeRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(false);

        when(employeeRepository.saveAndFlush(any(Employee.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violated"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> employeeService.create(request)
        );

        verify(employeeRepository)
                .existsByEmailIgnoreCase(request.email());

        verify(employeeRepository)
                .saveAndFlush(any(Employee.class));
    }
}