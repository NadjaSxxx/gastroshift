CREATE TABLE shifts
(
    id           UUID            PRIMARY KEY ,
    start_time   TIMESTAMP       NOT NULL ,
    end_time     TIMESTAMP       NOT NULL ,
    position     VARCHAR(100)    NOT NULL ,
    notes        VARCHAR(500),

    CONSTRAINT shifts_end_after_start
        CHECK ( end_time > start_time )
);



