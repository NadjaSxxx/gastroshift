package com.github.nadjasxxx.gastroshift.unavailability;

import com.github.nadjasxxx.gastroshift.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_unavailability")
public class EmployeeUnavailability {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalDateTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalDateTime endTime;

    @Column(length = 500)
    private String notes;

    protected EmployeeUnavailability() {
    }

    public EmployeeUnavailability(
            UUID id,
            Employee employee,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String notes
    ) {
        this.id = id;
        this.employee = employee;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getNotes() {
        return notes;
    }
}