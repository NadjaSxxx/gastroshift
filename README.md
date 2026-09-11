# GastroShift

GastroShift is a REST API for managing employees and work shifts in a hospitality business. It supports employee and shift management, shift assignments, overlap prevention, and date-range filtering.

The project is currently under active development as a portfolio and learning project.

## Features

### Employees

* Create and list employees
* Validate employee input
* Enforce case-insensitive unique email addresses
* Activate and deactivate employees
* Return structured errors for invalid or conflicting requests

### Shifts

* Create and list shifts
* Filter shifts by date and time range
* Validate that the end time is after the start time
* Assign one employee to a shift
* Remove an employee assignment
* Prevent overlapping assignments for the same employee
* Prevent inactive employees from receiving new assignments

### Technical Features

* PostgreSQL persistence
* Database versioning with Flyway
* JPA and Hibernate entity mapping
* Structured JSON error responses
* Integration tests with Spring MockMvc
* PostgreSQL integration tests with Testcontainers
* Docker Compose development environment

## Technology Stack

* Java 21
* Spring Boot 4.1.0
* Spring Web MVC
* Spring Data JPA
* Hibernate
* PostgreSQL 18.4
* Flyway
* Docker Compose
* Maven Wrapper
* JUnit
* MockMvc
* Testcontainers

## Requirements

Install the following tools before running the project:

* Java 21
* Docker Desktop
* Git

A separate Maven installation is not required because the project includes the Maven Wrapper.

## Configuration

Create a `.env` file in the project root:

```properties
POSTGRES_DB=gastroshift
POSTGRES_USER=gastroshift
POSTGRES_PASSWORD=change-me
```

Do not commit `.env` to Git.

The application imports this file through:

```properties
spring.config.import=optional:file:.env[.properties]
```

The API runs on:

```text
http://localhost:8081
```

PostgreSQL is exposed locally on port `5432`.

## Running PostgreSQL

Start the database container:

```bash
docker compose up -d
```

Check its status:

```bash
docker compose ps
```

The PostgreSQL service should report:

```text
healthy
```

View its logs:

```bash
docker compose logs postgres
```

Stop the container without deleting its data:

```bash
docker compose stop
```

Avoid the following command unless you intentionally want to delete the local database volume:

```bash
docker compose down -v
```

## Running the Application

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS and Linux

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

Flyway applies pending database migrations automatically during startup. Hibernate validates that the entity model matches the database schema.

## Running the Tests

Docker must be running because the integration tests start a temporary PostgreSQL container through Testcontainers.

### Windows

```powershell
.\mvnw.cmd test
```

### macOS and Linux

```bash
./mvnw test
```

The test database is separate from the local development database.

## API Endpoints

### Employees

| Method  | Endpoint                             | Description                        |
|---------|--------------------------------------|------------------------------------|
| `GET`   | `/api/employees`                     | List all employees                 |
| `POST`  | `/api/employees`                     | Create an employee                 |
| `PATCH` | `/api/employees/{employeeId}/status` | Activate or deactivate an employee |
| `PUT`   | `/api/employees/{employeeId}`        | Update an existing employee        |
| `GET`   | `/api/employees/{employeeId}`        | Get one employee                   |

### Shifts

| Method   | Endpoint                                      | Description                          |
|----------|-----------------------------------------------|--------------------------------------|
| `GET`    | `/api/shifts`                                 | List all shifts                      |
| `GET`    | `/api/shifts?from={from}&to={to}`             | List shifts overlapping a time range |
| `POST`   | `/api/shifts`                                 | Create a shift                       |
| `PUT`    | `/api/shifts/{shiftId}/employee/{employeeId}` | Assign an employee to a shift        |
| `DELETE` | `/api/shifts/{shiftId}/employee`              | Remove the employee assignment       |
| `PUT`    | `/api/shifts/{shiftId}`                       | Update an existing shift             |
| `DELETE` | `/api/shifts/{shiftId}`                       | Delete an existing shift             |
| `GET`    | `/api/shifts/{shiftId}`                       | Get one shift                        |

## Request Examples

### Create an Employee

```http
POST /api/employees
Content-Type: application/json
```

```json
{
  "firstName": "Mira",
  "lastName": "Beispiel",
  "email": "mira.beispiel@example.com"
}
```

A newly created employee is active by default.

### Change Employee Status

```http
PATCH /api/employees/{employeeId}/status
Content-Type: application/json
```

```json
{
  "active": false
}
```

### Update an Employee

```http
PUT /api/employees/{employeeId}
Content-Type: application/json
```

```json
{
  "firstName": "Mira",
  "lastName": "Muster",
  "email": "mira.muster@example.com"
}
```

Updating employee details preserves the employee ID and active status. Email addresses must remain unique regardless of letter case.

### Get an Employee

```http
GET /api/employees/{employeeId}
```

### Create a Shift

```http
POST /api/shifts
Content-Type: application/json
```

```json
{
  "startTime": "2026-09-10T10:00:00",
  "endTime": "2026-09-10T16:00:00",
  "position": "Service",
  "notes": "Terrace"
}
```

`notes` is optional. The end time must be after the start time.

### Get a Shift

```http
GET /api/shifts/{shiftId}
```

The shift response includes its employee assignment when one exists.

### Filter Shifts

```http
GET /api/shifts?from=2026-09-07T00:00:00&to=2026-09-14T00:00:00
```

A shift is included when it overlaps the requested range:

```text
shift.startTime < to
and
shift.endTime > from
```

Shifts that only touch the range boundary are excluded.

### Assign an Employee

```http
PUT /api/shifts/{shiftId}/employee/{employeeId}
```

The assignment is rejected when:

* the employee or shift does not exist,
* the employee is inactive,
* the employee already has an overlapping shift.

### Remove an Assignment

```http
DELETE /api/shifts/{shiftId}/employee
```

A successful request returns `204 No Content`. Repeating the request for an already unassigned shift also succeeds.

### Update a Shift

```http
PUT /api/shifts/{shiftId}
Content-Type: application/json
```

```json
{
  "startTime": "2026-09-15T12:00:00",
  "endTime": "2026-09-15T18:00:00",
  "position": "Kitchen",
  "notes": "Updated shift"
}
```

### Delete a Shift

```http
DELETE /api/shifts/{shiftId}
```

A successful deletion returns `204 No Content`. Deleting an assigned shift removes only the shift; the employee remains stored.

## HTTP Status Codes

| Status            | Meaning                                                         |
| ----------------- | --------------------------------------------------------------- |
| `200 OK`          | Request completed successfully                                  |
| `201 Created`     | Resource created successfully                                   |
| `204 No Content`  | Assignment removed successfully                                 |
| `400 Bad Request` | Input or time range is invalid                                  |
| `404 Not Found`   | Employee or shift does not exist                                |
| `409 Conflict`    | Email, status, or shift assignment conflicts with existing data |

Error responses use a consistent structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Employee already has an overlapping shift",
  "timestamp": "2026-09-10T12:00:00Z"
}
```

## Database Migrations

Flyway migrations are stored in:

```text
src/main/resources/db/migration
```

Current migrations:

| Version | Purpose                                         |
| ------- | ----------------------------------------------- |
| `V1`    | Create the employees table                      |
| `V2`    | Enforce case-insensitive unique employee emails |
| `V3`    | Create the shifts table                         |
| `V4`    | Add employee assignments to shifts              |

Existing migrations must not be modified after they have been applied. Schema changes should be introduced through new migration files.

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.github.nadjasxxx.gastroshift
│   │       ├── employee
│   │       └── shift
│   └── resources
│       └── db
│           └── migration
└── test
    └── java
        └── com.github.nadjasxxx.gastroshift
            ├── employee
            └── shift
```

The code is organized by business domain:

* `employee` contains employee-related entities, repositories, services, controllers, requests, and exceptions.
* `shift` contains shift-related entities, repositories, services, controllers, requests, and exceptions.

## Current Status

GastroShift currently provides a tested backend API. It does not yet include a frontend or authentication.

Planned improvements include:

* employee availability,
* response DTOs,
* authentication and authorization,
* weekly schedule views,
* a web frontend,
* deployment and production configuration.

## License

No license has been selected yet.
