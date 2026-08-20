ALTER TABLE shifts
    ADD COLUMN  employee_id UUID;

ALTER TABLE  shifts
    ADD CONSTRAINT  shifts_employee_fk
        FOREIGN KEY (employee_id)
            REFERENCES employees (id);

CREATE INDEX  shifts_employee_id_idx
    ON shifts (employee_id);