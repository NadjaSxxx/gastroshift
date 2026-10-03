package com.github.nadjasxxx.gastroshift.selfservice;

public class EmployeeIdentityNotLinkedException extends RuntimeException{

    public EmployeeIdentityNotLinkedException() {
        super("No employee is linked to the authenticated identity");
    }
}
