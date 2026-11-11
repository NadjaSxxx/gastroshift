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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@Tag(
        name = "Shifts",
        description = "Create, update and assign work shifts"
)
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @Operation(
            summary = "List shifts",
            description = """
                Returns all shifts or only shifts overlapping the requested
                time range. The from and to parameters must either both be
                provided or both be omitted. Requires the MANAGER role.
                """
    )
    @GetMapping
    public List<ShiftResponse> findAll(
            @Parameter(
                    description = """
                        Start of the requested time range in ISO 8601 format.
                        Must be provided together with to.
                        """
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,


            @Parameter(
                    description = """
                        End of the requested time range in ISO 8601 format.
                        Must be after from.
                        """
            )
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

    @Operation(
            summary = "Create a shift",
            description = """
                Creates an unassigned shift. The end time must be after the
                start time. Requires the MANAGER role.
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse create(
            @Valid @RequestBody CreateShiftRequest request
    ) {
        return ShiftResponse.from(
                shiftService.create(request)
        );
    }

    @Operation(
            summary = "Assign an employee to a shift",
            description = """
                Assigns an active employee to an existing shift. The
                assignment is rejected if it overlaps another assigned shift
                or an unavailability period. Requires the MANAGER role.
                """
    )
    @PutMapping("/{shiftId}/employee/{employeeId}")
    public ShiftResponse assignEmployee(
            @PathVariable UUID shiftId,
            @PathVariable UUID employeeId
    ) {
        return ShiftResponse.from(
                shiftService.assignEmployee(shiftId, employeeId)
        );
    }

    @Operation(
            summary = "Remove an employee assignment",
            description = """
                Removes the current employee assignment from a shift.
                Requires the MANAGER role.
                """
    )
    @DeleteMapping("/{shiftId}/employee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignEmployee(@PathVariable UUID shiftId) {
        shiftService.unassignEmployee(shiftId);
    }

    @Operation(
            summary = "Update a shift",
            description = """
                Updates the time range, position and notes of an existing
                shift while preserving its employee assignment. Existing
                overlap rules are validated again. Requires the MANAGER role.
                """
    )
    @PutMapping("/{shiftId}")
    public ShiftResponse update(
            @PathVariable UUID shiftId,
            @Valid @RequestBody UpdateShiftRequest request
    ) {
        return ShiftResponse.from(
                shiftService.update(shiftId, request)
        );
    }

    @Operation(
            summary = "Delete a shift",
            description = """
                Permanently deletes an existing shift.
                Requires the MANAGER role.
                """
    )
    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID shiftId) {
        shiftService.delete(shiftId);
    }

    @Operation(
            summary = "Get a shift by ID",
            description = """
                Returns one shift including its employee assignment when an
                assignment exists. Requires the MANAGER role.
                """
    )
    @GetMapping("/{shiftId}")
    public ShiftResponse findById(@PathVariable UUID shiftId) {
        return ShiftResponse.from(
                shiftService.findById(shiftId)
        );
    }
}
