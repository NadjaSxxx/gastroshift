package com.github.nadjasxxx.gastroshift.unavailability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeUnavailabilityRepository
        extends JpaRepository<EmployeeUnavailability, UUID> {

    List<EmployeeUnavailability>
    findByEmployee_IdOrderByStartTimeAsc(UUID employeeId);

    Optional<EmployeeUnavailability>
    findByIdAndEmployee_Id(
            UUID unavailabilityId,
            UUID employeeId
    );

    @Query("""
            SELECT CASE WHEN COUNT(unavailability) > 0
                        THEN true
                        ELSE false
                   END
            FROM EmployeeUnavailability unavailability
            WHERE unavailability.employee.id = :employeeId
              AND unavailability.startTime < :endTime
              AND unavailability.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("employeeId") UUID employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT unavailability
        FROM EmployeeUnavailability unavailability
        WHERE unavailability.employee.id = :employeeId
          AND unavailability.startTime < :to
          AND unavailability.endTime > :from
        ORDER BY unavailability.startTime
        """)
    List<EmployeeUnavailability>
    findOverlappingRangeForEmployee(
            @Param("employeeId") UUID employeeId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}