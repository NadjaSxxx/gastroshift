ALTER TABLE employees
    DROP CONSTRAINT employees_email_key;

CREATE UNIQUE INDEX employees_email_unique_ignore_case
    ON employees (LOWER(email));