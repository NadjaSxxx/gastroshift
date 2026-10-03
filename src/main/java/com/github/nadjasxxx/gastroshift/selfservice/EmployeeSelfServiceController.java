package com.github.nadjasxxx.gastroshift.selfservice;

import com.github.nadjasxxx.gastroshift.shift.Shift;
import com.github.nadjasxxx.gastroshift.shift.ShiftResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class EmployeeSelfServiceController {

    private final EmployeeSelfService employeeSelfService;

    public EmployeeSelfServiceController(
            EmployeeSelfService employeeSelfService
    ) {
        this.employeeSelfService = employeeSelfService;
    }

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