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
* Link employee records to external identity subjects
* Create, list, and delete personal unavailability periods
* Allow managers to view employee unavailability for a requested time range

### Shifts

* Create, list, retrieve, update, and delete shifts
* Filter shifts by date and time range
* Validate that the end time is after the start time
* Assign one employee to a shift
* Remove an employee assignment
* Prevent overlapping assignments for the same employee
* Prevent inactive employees from receiving new assignments
* Allow authenticated employees to retrieve their own assigned shifts
* Prevent assignments and updates during employee unavailability

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
* Role-based authorization for management endpoints
* OpenAPI 3 documentation generated with springdoc-openapi
* Interactive Swagger UI with Keycloak authentication using Authorization Code Flow and PKCE

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
* springdoc-openapi
* Swagger UI

## Requirements

Install the following tools before running the project:

* Java 21 with current security updates
* Docker Desktop
* Git

A separate Maven installation is not required because the project includes the Maven Wrapper.


## Quick Start for Reviewers

Clone the repository and enter the project directory:

```bash
git clone https://github.com/NadjaSxxx/gastroshift.git
cd gastroshift
```

Create the local environment file.

### Windows

```powershell
Copy-Item .env.example .env
```

### macOS and Linux

```bash
cp .env.example .env
```

Open `.env` and replace every `change-me` value with a local password. Use different passwords for the Keycloak administrator, manager demo user and employee demo user.

Start PostgreSQL and Keycloak:

```bash
docker compose up -d
```

Check the container status:

```bash
docker compose ps
```

During its first startup, Keycloak automatically imports the `gastroshift` realm, its roles, the Swagger UI client and the demo users.

Start the application.

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS and Linux

```bash
./mvnw spring-boot:run
```

Open Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

Select **Authorize**, enable the `openid` scope and sign in as:

```text
manager-demo
```

Use the password configured through `KEYCLOAK_MANAGER_DEMO_PASSWORD` in `.env`.

The manager demo user can create employees, shifts and other application data through Swagger UI. No client secret is required for the browser-based login.

## Configuration

Create a `.env` file in the project root by copying `.env.example`.

The file contains the following configuration:

```properties
POSTGRES_DB=gastroshift
POSTGRES_USER=gastroshift
POSTGRES_PASSWORD=change-me
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=change-me
KEYCLOAK_MANAGER_DEMO_PASSWORD=change-me
KEYCLOAK_EMPLOYEE_DEMO_PASSWORD=change-me
```

Replace all `change-me` values before starting the development environment.

Use separate passwords for:

* PostgreSQL
* the Keycloak bootstrap administrator
* the manager demo user
* the employee demo user

Do not commit `.env` to Git. The committed `.env.example` file contains placeholders only.

The application imports `.env` through:

```properties
spring.config.import=optional:file:.env[.properties]
```

The local services use the following addresses:

| Service         | Address                                       |
|-----------------|-----------------------------------------------|
| GastroShift API | `http://localhost:8081`                       |
| Swagger UI      | `http://localhost:8081/swagger-ui/index.html` |
| Keycloak        | `http://localhost:8080`                       |
| PostgreSQL      | `localhost:5432`                              |

The Keycloak administration console uses the bootstrap administrator credentials configured in `.env`.


## Running the Development Infrastructure

Start PostgreSQL and Keycloak:

```bash
docker compose up -d
```
On the first startup of a fresh Keycloak data volume, Keycloak imports `keycloak/gastroshift-realm.json`. The import creates the realm, application roles, Swagger UI client and local demo users. If the realm already exists, Keycloak skips the import and preserves the existing configuration.

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

Keycloak automatically imports the local development configuration from:

```text
keycloak/gastroshift-realm.json
```

The import runs when Keycloak starts with a fresh data volume. If the `gastroshift` realm already exists, Keycloak skips the import and preserves the existing realm.

The OpenID Connect configuration is published at:

```text
http://localhost:8080/realms/gastroshift/.well-known/openid-configuration
```

The Keycloak administration console is available at:

```text
http://localhost:8080
```

Sign in to the administration console with the bootstrap administrator credentials configured in `.env`.

### Imported development configuration

The realm import creates:

* the `gastroshift` realm
* the `MANAGER` realm role
* the `EMPLOYEE` realm role
* the public `gastroshift-swagger` client
* the `manager-demo` user
* the `employee-demo` user

The Swagger client uses Authorization Code Flow with PKCE and does not require a client secret.

The demo identities are configured as follows:

| Username | Realm role | Password source | Keycloak subject |
|---|---|---|---|
| `manager-demo` | `MANAGER` | `KEYCLOAK_MANAGER_DEMO_PASSWORD` | `10000000-0000-0000-0000-000000000001` |
| `employee-demo` | `EMPLOYEE` | `KEYCLOAK_EMPLOYEE_DEMO_PASSWORD` | `10000000-0000-0000-0000-000000000002` |

The actual passwords are read from the local `.env` file during the initial realm import. Passwords and client secrets are not stored in the committed realm file.

### Linking the employee demo identity

The imported `employee-demo` user is initially not linked to an employee in the GastroShift database.

First, sign in to Swagger UI as `manager-demo` and create an employee through:

```http
POST /api/employees
```

Copy the returned employee ID. Then call:

```http
PUT /api/employees/{employeeId}/identity
Content-Type: application/json
```

Use the database employee ID as the `employeeId` path parameter and submit:

```json
{
  "identitySubject": "10000000-0000-0000-0000-000000000002"
}
```

After the identity is linked, sign in as `employee-demo` to use the endpoints below:

```text
/api/me/**
```

The database employee ID and the Keycloak subject are different identifiers:

* the employee ID identifies the business record in PostgreSQL
* the Keycloak subject identifies the authenticated user

### Existing Keycloak data volumes

The startup import does not overwrite an existing `gastroshift` realm. This protects manually created users, clients and other local configuration.

Changing `keycloak/gastroshift-realm.json` therefore does not modify a realm that has already been imported. Testing a fresh import requires a new Keycloak data volume.

Do not use the following command merely to repeat the import:

```bash
docker compose down -v
```

It deletes both the local Keycloak configuration and the local PostgreSQL data.

### Optional technical test clients

The imported Swagger client is sufficient for interactive browser testing. The following technical clients are optional and are only required for testing the API through the Client Credentials flow.

Create a confidential OpenID Connect client with:

* Client ID: `gastroshift-api-test-client`
* Client authentication: enabled
* Authorization: disabled
* Standard flow: disabled
* Direct access grants: disabled
* Service accounts roles: enabled

Assign the `MANAGER` realm role to its service account.

The client secret is available on the client's **Credentials** tab. Treat it as a password and do not commit it to Git.

To test employee endpoints through a service account, create a second technical client named:

```text
gastroshift-employee-test-client
```

Use the same technical-client settings and assign only the `EMPLOYEE` realm role to its service account.

Technical clients represent applications or automated processes. The imported demo users represent people signing in interactively through Swagger UI.


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

## Exploring the API with Swagger UI

After starting the development infrastructure and the application, open:

```text
http://localhost:8081/swagger-ui/index.html
```

The generated OpenAPI document is available at:

```text
http://localhost:8081/v3/api-docs
```

Swagger UI displays the available endpoints, request schemas, response schemas, validation requirements and important business rules.

### Testing manager endpoints

Select **Authorize**, enable the `openid` scope and sign in with:

```text
Username: manager-demo
Password: value of KEYCLOAK_MANAGER_DEMO_PASSWORD from .env
```

The manager user can access management endpoints such as:

```text
/api/employees
/api/shifts
/api/employees/{employeeId}/unavailability
```

Swagger UI automatically includes the access token in protected requests.

### Testing employee self-service

Before testing employee self-service, link `employee-demo` to an employee record as described in `Linking the employee demo identity`.

Sign out of the manager authorization and sign in with:

```text
Username: employee-demo
Password: value of KEYCLOAK_EMPLOYEE_DEMO_PASSWORD from .env
```

The employee user can access:

```text
/api/me/shifts
/api/me/unavailability
```

The employee user cannot access management endpoints. A request to `/api/employees` is expected to return:

```text
403 Forbidden
```

If Keycloak automatically reuses the previous user's browser session, use a private browser window or a separate browser for the second user.

### Authentication behavior

Swagger UI uses Authorization Code Flow with PKCE. The public browser client does not use a client secret.

Requests without a valid access token receive:

```text
401 Unauthorized
```

Authenticated requests without the required realm role receive:

```text
403 Forbidden
```

Swagger UI is an interactive developer interface for exploring the REST API. It is not an end-user frontend for restaurant managers or employees.

## Calling the Authenticated API

The following command-line example is optional. It requires the manually configured `gastroshift-api-test-client` described under `Optional technical test clients`. The automatically imported `gastroshift-swagger` client should be used for interactive browser testing.

All endpoints below `/api` require a valid access token issued by the `gastroshift` Keycloak realm.

Endpoints below `/api/me/**` require the `EMPLOYEE` realm role. The remaining management endpoints below `/api/**` require the `MANAGER` realm role.

For local command-line testing, request a manager access token in PowerShell. Replace the placeholder with the client secret from Keycloak:

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

Send the access token in the HTTP `Authorization` header:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8081/api/employees" `
    -Headers @{
        Authorization = "Bearer $accessToken"
    }
```

Requests without a valid access token receive HTTP status `401 Unauthorized`. Authenticated requests without the required role receive HTTP status `403 Forbidden`.

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

| Method  | Endpoint                                                         | Description                                           |
|---------|------------------------------------------------------------------|-------------------------------------------------------|
| `GET`   | `/api/employees`                                                 | List all employees                                    |
| `POST`  | `/api/employees`                                                 | Create an employee                                    |
| `PATCH` | `/api/employees/{employeeId}/status`                             | Activate or deactivate an employee                    |
| `PUT`   | `/api/employees/{employeeId}`                                    | Update an existing employee                           |
| `PUT`   | `/api/employees/{employeeId}/identity`                           | Link an employee to an identity subject               |
| `GET`   | `/api/employees/{employeeId}`                                    | Get one employee                                      |
| `GET`   | `/api/employees/{employeeId}/unavailability?from={from}&to={to}` | List employee unavailability overlapping a time range |

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

### Employee Self-Service

| Method   | Endpoint                                      | Description                             |
|----------|-----------------------------------------------|-----------------------------------------|
| `GET`    | `/api/me/shifts`                              | List the authenticated employee's shifts|
| `GET`    | `/api/me/unavailability`                      | List personal unavailability periods    |
| `POST`   | `/api/me/unavailability`                      | Create a personal unavailability period |
| `DELETE` | `/api/me/unavailability/{unavailabilityId}`   | Delete a personal unavailability period |

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

### Link an Employee Identity

```http
PUT /api/employees/{employeeId}/identity
Content-Type: application/json
```

```json
{
  "identitySubject": "keycloak-subject-value"
}
```

The identity subject must be unique. Repeating the same link for the same employee is allowed.

### Get Own Shifts

```http
GET /api/me/shifts
```

The employee is identified through the JWT `sub` claim. No employee ID is accepted from the request.

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

A successful deletion returns `204 No Content`. Deleting an assigned shift removes only the shift; the employee remains stored.

```http
DELETE /api/shifts/{shiftId}
```

### Create Own Unavailability

```http
POST /api/me/unavailability
Content-Type: application/json
```

```json
{
  "startTime": "2026-11-10T08:00:00",
  "endTime": "2026-11-10T16:00:00",
  "notes": "Medical appointment"
}
```

The end time must be after the start time. The period must not overlap another unavailability period or an already assigned shift.

### Get Own Unavailability

```http
GET /api/me/unavailability
```

Results are ordered by start time.

### Delete Own Unavailability

```http
DELETE /api/me/unavailability/{unavailabilityId}
```

A successful deletion returns `204 No Content`. Employees cannot delete another employee's unavailability.

### Get Employee Unavailability as Manager

```http
GET /api/employees/{employeeId}/unavailability?from=2026-11-10T00:00:00&to=2026-11-11T00:00:00
```

Both `from` and `to` are required, and `to` must be after `from`. Only unavailability periods overlapping the requested range are returned. Results are ordered by start time.

This endpoint requires the `MANAGER` role.

## HTTP Status Codes

| Status             | Meaning                                                         |
|--------------------|-----------------------------------------------------------------|
| `200 OK`           | Request completed successfully                                  |
| `201 Created`      | Resource created successfully                                   |
| `204 No Content`   | Request completed successfully without a response body          |
| `400 Bad Request`  | Input or time range is invalid                                  |
| `401 Unauthorized` | Access token is missing, invalid, expired, or not accepted      |
| `403 Forbidden`    | Authentication succeeded, but the required role is missing      |
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
|---------|-------------------------------------------------|
| `V1`    | Create the employees table                      |
| `V2`    | Enforce case-insensitive unique employee emails |
| `V3`    | Create the shifts table                         |
| `V4`    | Add employee assignments to shifts              |
| `V5`    | Add unique identity subjects to employees       |
| `V6`    | Create the employee unavailability table        |

Existing migrations must not be modified after they have been applied. Schema changes should be introduced through new migration files.

## Project Structure

```text
keycloak/
  gastroshift-realm.json

src/
  main/
    java/
      com.github.nadjasxxx.gastroshift/
        documentation/
        employee/
        security/
        selfservice/
        shift/
        unavailability/
    resources/
      db/
        migration/
      application.properties

  test/
    java/
      com.github.nadjasxxx.gastroshift/
        employee/
        security/
        selfservice/
        shift/
        unavailability/
```

The code is organized by business domain:

* `documentation` contains the OpenAPI and Swagger UI configuration.
* `employee` contains employee-related entities, repositories, services, controllers, requests, responses and exceptions.
* `security` contains the Spring Security, JWT and Keycloak role-mapping configuration.
* `selfservice` contains endpoints and services for the currently authenticated employee.
* `shift` contains shift-related entities, repositories, services, controllers, requests, responses and exceptions.
* `unavailability` contains employee unavailability entities, repositories, services, controllers, requests, responses and exceptions.
* `db/migration` contains the versioned Flyway database migrations.
* `keycloak/gastroshift-realm.json` contains the reproducible local Keycloak development configuration.

## Current Status

GastroShift currently provides a tested REST backend for managing employees, shifts, employee assignments and employee unavailability.

The application currently supports:

* PostgreSQL persistence with Flyway database migrations
* employee creation, updates, status management and identity linking
* shift creation, updates, deletion and employee assignment
* prevention of overlapping shifts
* prevention of assignments during employee unavailability
* manager access to employee unavailability with time-range filtering
* employee self-service access to assigned shifts
* employee self-service management of unavailability periods
* Keycloak authentication with `MANAGER` and `EMPLOYEE` realm roles
* OpenAPI documentation and authenticated Swagger UI
* reproducible local Keycloak configuration with automatically imported demo users
* integration tests with MockMvc and Testcontainers
* automated builds and tests with GitHub Actions

GastroShift does not yet include an end-user frontend or a public deployment.

Planned improvements include:

* recurring weekly availability
* weekly schedule views
* automatic creation of optional demo business data
* a web frontend for managers and employees
* deployment and production configuration

## License

No license has been selected yet.
