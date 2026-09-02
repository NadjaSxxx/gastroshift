package com.github.nadjasxxx.gastroshift.shift;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    @Query("""
        SELECT COUNT(shift) > 0
        FROM Shift shift
        WHERE shift.employee.id = :employeeId
          AND shift.id <> :shiftId
          AND shift.startTime < :endTime
          AND shift.endTime > :startTime
        """)
        boolean existsOverlappingShift(
            @Param("employeeId") UUID employeeId,
            @Param("shiftId") UUID shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT shift
        FROM Shift shift
        WHERE shift.startTime < :to
          AND shift.endTime > :from
        ORDER BY shift.startTime
        """)
    List<Shift> findOverlappingRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
