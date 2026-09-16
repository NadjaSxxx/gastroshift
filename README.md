# GastroShift

GastroShift is a REST API for managing employees and work shifts in a hospitality business. It supports employee and shift management, shift assignments, overlap prevention, and date-range filtering.

The project is currently under active development as a portfolio and learning project.

## Features

### Employees

* Create, list, retrieve, and update employees
* Validate employee input
* Enforce case-insensitive unique email addresses
* Activate and deactivate employees
* Return structured errors for invalid or conflicting requests

### Shifts

* Create, list, retrieve, update, and delete shifts
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
* OAuth 2.0 resource server authentication
* JWT validation with Keycloak
* Dedicated API response DTOs

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
* Spring Security
* Keycloak 26.7.3

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
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=change-me
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

Keycloak is available locally on:

```text
http://localhost:8080
```

The Keycloak administration console uses the bootstrap administrator credentials configured in `.env`.

## Running the Development Infrastructure

Start PostgreSQL and Keycloak:

```bash
docker compose up -d
```

Check their status:

```bash
docker compose ps
```

The PostgreSQL service should report `healthy`. Keycloak may require a little more time during its first startup.

View the service logs:

```bash
docker compose logs postgres
docker compose logs keycloak
```

Stop both services without deleting their data:

```bash
docker compose stop
```

Avoid the following command unless you intentionally want to delete both the local PostgreSQL data and the local Keycloak configuration:

```bash
docker compose down -v
```

## Configuring Keycloak

Open the Keycloak administration console:

```text
http://localhost:8080
```

Sign in with the bootstrap administrator credentials configured in `.env`.

Create a realm named:

```text
gastroshift
```

The realm publishes its OpenID Connect configuration at:

```text
http://localhost:8080/realms/gastroshift/.well-known/openid-configuration
```

Inside the `gastroshift` realm, create an OpenID Connect client with the following settings:

* Client ID: `gastroshift-api-test-client`
* Client authentication: enabled
* Authorization: disabled
* Standard flow: disabled
* Direct access grants: disabled
* Service accounts roles: enabled

Root URL, Home URL, redirect URIs, and web origins can remain empty because this technical client does not use a browser-based login flow.

The client secret is available on the client's **Credentials** tab. Treat it as a password and do not commit it to Git.

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

## Calling the Authenticated API

All endpoints below `/api` require a valid access token issued by the `gastroshift` Keycloak realm.

For local testing, request an access token in PowerShell. Replace the placeholder with the client secret from Keycloak:

```powershell
$tokenResponse = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/realms/gastroshift/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        grant_type    = "client_credentials"
        client_id     = "gastroshift-api-test-client"
        client_secret = "HIER_CLIENT_SECRET_EINSETZEN"
    }

$accessToken = $tokenResponse.access_token
```

Send the token in the HTTP `Authorization` header:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8081/api/employees" `
    -Headers @{
        Authorization = "Bearer $accessToken"
    }
```

Requests without a valid token receive HTTP status `401 Unauthorized`.

## Running the Tests

Docker must be running because the integration tests start a temporary PostgreSQL container through Testcontainers.

The automated tests mock JWT processing and therefore do not require a running Keycloak instance.

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

All examples require the following HTTP header. It is omitted from the individual examples for readability:

```http
Authorization: Bearer <access-token>
```

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

| Status             | Meaning                                                         |
|--------------------|-----------------------------------------------------------------|
| `200 OK`           | Request completed successfully                                  |
| `201 Created`      | Resource created successfully                                   |
| `204 No Content`   | Request completed successfully without a response body          |
| `400 Bad Request`  | Input or time range is invalid                                  |
| `401 Unauthorized` | Access token is missing, invalid, expired, or not accepted      |
| `404 Not Found`    | Employee or shift does not exist                                |
| `409 Conflict`     | Email, status, or shift assignment conflicts with existing data |

Domain and validation error responses use a consistent structure:

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
│   │       ├── security
│   │       └── shift
│   └── resources
│       └── db
│           └── migration
└── test
    └── java
        └── com.github.nadjasxxx.gastroshift
            ├── employee
            ├── security
            └── shift
```

The code is organized by business domain:

* `employee` contains employee-related entities, repositories, services, controllers, requests, and exceptions.
* `security` contains the Spring Security and OAuth 2.0 resource server configuration.
* `shift` contains shift-related entities, repositories, services, controllers, requests, and exceptions.

## Current Status

GastroShift currently provides a tested backend API with PostgreSQL persistence, dedicated response DTOs, and Keycloak-based authentication. It does not yet include role-based authorization or a frontend.

Planned improvements include:

* role-based authorization,
* mapping Keycloak users to employees,
* employee availability,
* weekly schedule views,
* a web frontend,
* deployment and production configuration.

## License

No license has been selected yet.
