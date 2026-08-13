package com.github.nadjasxxx.gastroshift.shift;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> findAll() {
        return shiftRepository.findAll();
    }

    public Shift create(CreateShiftRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidShiftTimeRangeException();
        }
        Shift shift = new Shift(
                UUID.randomUUID(),
                request.startTime(),
                request.endTime(),
                request.position(),
                request.notes()
        );
        return shiftRepository.saveAndFlush(shift);
    }
}
