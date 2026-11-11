package com.github.nadjasxxx.gastroshift.selfservice;

import com.github.nadjasxxx.gastroshift.shift.Shift;
import com.github.nadjasxxx.gastroshift.shift.ShiftResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@Tag(
        name = "Employee Self-Service",
        description = "Access resources belonging to the authenticated employee"
)
public class EmployeeSelfServiceController {

    private final EmployeeSelfService employeeSelfService;

    public EmployeeSelfServiceController(
            EmployeeSelfService employeeSelfService
    ) {
        this.employeeSelfService = employeeSelfService;
    }

    @Operation(
            summary = "List my shifts",
            description = """
                Returns the shifts assigned to the authenticated employee.
                The employee is identified through the subject claim of the
                access token, so no employee ID is supplied by the caller.
                Requires the EMPLOYEE role and a linked employee identity.
                """
    )
    @GetMapping("/shifts")
    public List<ShiftResponse> findOwnShifts(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<Shift> shifts = employeeSelfService.findOwnShifts(
                jwt.getSubject()
        );

        return shifts.stream()
                .map(ShiftResponse::from)
                .toList();
    }
}