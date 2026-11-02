CREATE TABLE employee_unavailability
(
    id          UUID PRIMARY KEY,
    employee_id UUID                        NOT NULL,
    start_time  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    notes       VARCHAR(500),

    CONSTRAINT employee_unavailability_end_after_start
        CHECK (end_time > start_time),

    CONSTRAINT employee_unavailability_employee_fk
        FOREIGN KEY (employee_id)
            REFERENCES employees (id)
);

CREATE INDEX employee_unavailability_employee_time_idx
    ON employee_unavailability (
                                employee_id,
                                start_time,
                                end_time
        );