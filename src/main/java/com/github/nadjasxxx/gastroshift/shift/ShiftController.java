package com.github.nadjasxxx.gastroshift.shift;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public List<Shift> findAll() {
        return shiftService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shift create(@Valid @RequestBody CreateShiftRequest request) {
        return shiftService.create(request);
    }
}
