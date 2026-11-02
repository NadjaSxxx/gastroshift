package com.github.nadjasxxx.gastroshift.unavailability;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/unavailability")
public class EmployeeUnavailabilityController {

    private final EmployeeUnavailabilityService
            unavailabilityService;

    public EmployeeUnavailabilityController(
            EmployeeUnavailabilityService
                    unavailabilityService
    ) {
        this.unavailabilityService = unavailabilityService;
    }

    @GetMapping
    public List<EmployeeUnavailabilityResponse> findOwn(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return unavailabilityService
                .findOwn(jwt.getSubject())
                .stream()
                .map(EmployeeUnavailabilityResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeUnavailabilityResponse createOwn(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            CreateUnavailabilityRequest request
    ) {
        EmployeeUnavailability created =
                unavailabilityService.createOwn(
                        jwt.getSubject(),
                        request
                );

        return EmployeeUnavailabilityResponse.from(created);
    }

    @DeleteMapping("/{unavailabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwn(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID unavailabilityId
    ) {
        unavailabilityService.deleteOwn(
                jwt.getSubject(),
                unavailabilityId
        );
    }
}