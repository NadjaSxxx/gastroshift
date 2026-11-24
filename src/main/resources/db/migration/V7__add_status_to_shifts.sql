ALTER TABLE shifts
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE shifts
    ADD CONSTRAINT shifts_status_valid
        CHECK (status IN ('DRAFT', 'PUBLISHED'));