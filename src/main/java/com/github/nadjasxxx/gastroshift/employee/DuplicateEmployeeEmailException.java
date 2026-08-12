package com.github.nadjasxxx.gastroshift.employee;

public class DuplicateEmployeeEmailException extends RuntimeException{

    public DuplicateEmployeeEmailException(String email) {
        super("An employee with email '%s' already exists".formatted(email));
    }
}
