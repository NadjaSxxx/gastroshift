ALTER TABLE employees
    ADD COLUMN identity_subject VARCHAR(255);

ALTER TABLE employees
    ADD CONSTRAINT employees_identity_subject_unique
        UNIQUE (identity_subject);