package com.github.nadjasxxx.gastroshift.shift;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
}
