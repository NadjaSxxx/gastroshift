package com.github.nadjasxxx.gastroshift.employee;

public class DuplicateEmployeeIdentityException extends RuntimeException{

    public DuplicateEmployeeIdentityException(String identitySubject) {
        super("Identity subject '%s' is already linked to an employee".formatted(identitySubject));
    }
}
