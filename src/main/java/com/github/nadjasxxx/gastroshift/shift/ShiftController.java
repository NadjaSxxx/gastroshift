package com.github.nadjasxxx.gastroshift.shift;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public List<ShiftResponse> findAll(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        List<Shift> shifts;

        if (from == null && to == null) {
            shifts = shiftService.findAll();
        } else {
            shifts = shiftService.findAll(from, to);
        }

        return shifts.stream()
                .map(ShiftResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse create(
            @Valid @RequestBody CreateShiftRequest request
    ) {
        return ShiftResponse.from(
                shiftService.create(request)
        );
    }

    @PutMapping("/{shiftId}/employee/{employeeId}")
    public ShiftResponse assignEmployee(
            @PathVariable UUID shiftId,
            @PathVariable UUID employeeId
    ) {
        return ShiftResponse.from(
                shiftService.assignEmployee(shiftId, employeeId)
        );
    }

    @DeleteMapping("/{shiftId}/employee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignEmployee(@PathVariable UUID shiftId) {
        shiftService.unassignEmployee(shiftId);
    }

    @PutMapping("/{shiftId}")
    public ShiftResponse update(
            @PathVariable UUID shiftId,
            @Valid @RequestBody UpdateShiftRequest request
    ) {
        return ShiftResponse.from(
                shiftService.update(shiftId, request)
        );
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID shiftId) {
        shiftService.delete(shiftId);
    }

    @GetMapping("/{shiftId}")
    public ShiftResponse findById(@PathVariable UUID shiftId) {
        return ShiftResponse.from(
                shiftService.findById(shiftId)
        );
    }
}
