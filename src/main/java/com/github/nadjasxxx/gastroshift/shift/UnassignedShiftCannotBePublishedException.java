package com.github.nadjasxxx.gastroshift.shift;

import java.util.UUID;

public class UnassignedShiftCannotBePublishedException extends RuntimeException {

    public UnassignedShiftCannotBePublishedException(UUID shiftId) {
        super(
                "Cannot publish unassigned shift: %s"
                        .formatted(shiftId)
        );
    }
}
