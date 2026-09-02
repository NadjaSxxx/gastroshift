package com.github.nadjasxxx.gastroshift.shift;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.nadjasxxx.gastroshift.employee.Employee;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;


    @Column(length = 100)
    private String position;

    @Column(length = 500)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;


    protected Shift() {
    }

    public Shift(UUID id, LocalDateTime startTime, LocalDateTime endTime, String position, String notes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.position = position;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getPosition() {
        return position;
    }

    public String getNotes() {
        return notes;
    }

    public Employee getEmployee() { return employee; }

    public void assignEmployee(Employee employee) {
        this.employee = employee;
    }

    public void unassignEmployee() {
        this.employee = null;
    }
}
