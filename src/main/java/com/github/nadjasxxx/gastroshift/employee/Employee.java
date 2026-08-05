package com.github.nadjasxxx.gastroshift.employee;

import java.util.UUID;

public class Employee {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean active;

    public Employee(UUID id, String firstName, String lastName, String email, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

}
