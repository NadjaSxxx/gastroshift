package com.github.nadjasxxx.gastroshift.shift;

import java.util.UUID;

public class ShiftNotFoundException extends RuntimeException{

    public ShiftNotFoundException(UUID id) {

        super("Shift not found: " + id);
    }
}
