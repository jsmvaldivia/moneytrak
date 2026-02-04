# Feature Specification: Simple Role-Based Authentication

**Feature Branch**: `003-simple-auth`
**Created**: 2026-02-03
**Status**: Draft
**Input**: User description: "Add simple auth to the application for now, keep it simple. One role for consuming applications and another for admin data." Updated: Three roles - ADMIN, APP, BACKOFFICE.

## Clarifications

### Session 2026-02-03

- Q: Should failed authentication attempts be logged for security monitoring? → A: Yes — log all failed authentication attempts including username, timestamp, and IP address.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Protect All Endpoints with Authentication (Priority: P1)

As a system owner, I need all existing endpoints to require valid credentials before granting access, so that unauthorized parties cannot read or modify financial data.

Currently, the MoneyTrak application exposes all transaction and category endpoints without any authentication. Any client can create, read, update, or delete transactions and categories. This story adds a credential gate in front of every endpoint so that only known clients can interact with the system.

**Why this priority**: Without authentication, the entire application is open to anyone. This is the foundational security layer that all other authorization depends on. No other story delivers value without this one.

**Independent Test**: Can be fully tested by sending requests without credentials and verifying they are rejected, then sending requests with valid credentials and verifying they succeed.

**Acceptance Scenarios**:

1. **Given** a client with no credentials, **When** they send a request to any protected endpoint (e.g., `GET /v1/transactions`), **Then** the system returns a 401 Unauthorized error response.
2. **Given** a client with valid credentials for any role, **When** they send a request to an endpoint they have access to, **Then** the system processes the request normally and returns the expected response.
3. **Given** a client with invalid credentials (wrong password), **When** they send a request to any endpoint, **Then** the system returns a 401 Unauthorized error response.
4. **Given** a client with a malformed or missing authentication header, **When** they send a request, **Then** the system returns a 401 Unauthorized error response.

---

### User Story 2 - APP Role for Consuming Applications (Priority: P2)

As a consuming application (e.g., a mobile app, dashboard, or reporting tool), I need to be able to read transaction and category data without the ability to modify it, so that I can display financial information to users without risk of accidental data changes.

The APP role grants access to all read-only operations: listing transactions, viewing individual transactions, listing categories, viewing individual categories, and accessing summary endpoints. APP clients cannot create, update, or delete any resources.

**Why this priority**: The primary use case for external integrations is reading data. Establishing this role ensures consuming applications have just enough access to fulfill their purpose, following the principle of least privilege.

**Independent Test**: Can be fully tested by authenticating as an APP user and verifying all GET endpoints succeed, then attempting POST/PUT/DELETE operations and verifying they are rejected with a 403 Forbidden error.

**Acceptance Scenarios**:

1. **Given** a client authenticated with the APP role, **When** they request a list of transactions (`GET /v1/transactions`), **Then** the system returns the list of transactions successfully.
2. **Given** a client authenticated with the APP role, **When** they request transaction summaries (e.g., `GET /v1/transactions/summary/expenses`), **Then** the system returns the summary data successfully.
3. **Given** a client authenticated with the APP role, **When** they request a list of categories (`GET /v1/categories`), **Then** the system returns the list of categories successfully.
4. **Given** a client authenticated with the APP role, **When** they request a single transaction or category by ID, **Then** the system returns the resource successfully.
5. **Given** a client authenticated with the APP role, **When** they attempt to create a transaction (`POST /v1/transactions`), **Then** the system returns a 403 Forbidden error response.
6. **Given** a client authenticated with the APP role, **When** they attempt to update or delete a transaction, **Then** the system returns a 403 Forbidden error response.
7. **Given** a client authenticated with the APP role, **When** they attempt to create, update, or delete a category, **Then** the system returns a 403 Forbidden error response.

---

### User Story 3 - BACKOFFICE Role for Data Management (Priority: P2)

As a back-office user, I need full access to create, read, update, and delete transactions and categories, so that I can manage the day-to-day financial data in the system.

The BACKOFFICE role grants full CRUD access to all transaction and category endpoints. Back-office users handle data entry, corrections, and category management as part of regular operations.

**Why this priority**: Same priority as APP because both roles define complementary halves of the access model. Without BACKOFFICE, no one can manage data after authentication is enabled.

**Independent Test**: Can be fully tested by authenticating as a BACKOFFICE user and verifying all CRUD endpoints (GET, POST, PUT, DELETE) succeed for both transactions and categories.

**Acceptance Scenarios**:

1. **Given** a client authenticated with the BACKOFFICE role, **When** they create a transaction (`POST /v1/transactions`), **Then** the system creates the transaction and returns a 201 response with a Location header.
2. **Given** a client authenticated with the BACKOFFICE role, **When** they read, update, or delete transactions, **Then** the system processes the operation normally.
3. **Given** a client authenticated with the BACKOFFICE role, **When** they perform any category operation (create, read, update, delete), **Then** the system processes the operation normally.
4. **Given** a client authenticated with the BACKOFFICE role, **When** they access summary endpoints, **Then** the system returns the data successfully.

---

### User Story 4 - ADMIN Role for Full System Access (Priority: P3)

As a system administrator, I need all the permissions of the BACKOFFICE role plus access to system management capabilities, so that I can oversee the entire application.

The ADMIN role grants everything BACKOFFICE has (full CRUD on transactions and categories) plus access to actuator endpoints (beyond health) for system monitoring and diagnostics. This is the highest privilege role.

**Why this priority**: ADMIN builds on BACKOFFICE. The additional actuator access is valuable for operations but not required for core business functionality.

**Independent Test**: Can be fully tested by authenticating as an ADMIN user and verifying full CRUD access plus actuator endpoint access.

**Acceptance Scenarios**:

1. **Given** a client authenticated with the ADMIN role, **When** they perform any transaction or category operation, **Then** the system processes the operation normally (same as BACKOFFICE).
2. **Given** a client authenticated with the ADMIN role, **When** they access actuator endpoints (e.g., `/actuator/info`), **Then** the system returns the actuator data successfully.
3. **Given** a client authenticated with the BACKOFFICE role, **When** they access actuator endpoints (beyond health), **Then** the system returns a 403 Forbidden error response.
4. **Given** a client authenticated with the APP role, **When** they access actuator endpoints (beyond health), **Then** the system returns a 403 Forbidden error response.

---

### User Story 5 - Health Check Remains Publicly Accessible (Priority: P3)

As an operations team member, I need the health check endpoint to remain accessible without credentials, so that infrastructure monitoring tools can verify the application is running without needing authentication.

The actuator health endpoint must be excluded from authentication requirements so that load balancers, uptime monitors, and deployment pipelines can check application liveness.

**Why this priority**: While not a core feature, this is essential for operational readiness. Without it, deploying the application with authentication would break standard monitoring setups.

**Independent Test**: Can be fully tested by sending a request to the health endpoint without credentials and verifying it returns a successful health status.

**Acceptance Scenarios**:

1. **Given** an unauthenticated client, **When** they request the health endpoint, **Then** the system returns the application health status successfully.
2. **Given** an unauthenticated client, **When** they request any other endpoint, **Then** the system returns a 401 Unauthorized error response.

---

### Edge Cases

- What happens when an APP user attempts a write operation? The system returns a 403 Forbidden error with a clear message indicating insufficient permissions.
- What happens when credentials are valid but the role is unrecognized? The system rejects the request as unauthorized.
- What happens when a request contains expired or tampered credentials? The system rejects the request as unauthorized.
- How does the system respond to concurrent requests with different roles? Each request is evaluated independently based on its own credentials; no cross-request interference occurs.
- What happens to existing tests that do not send credentials? They must be updated to include valid credentials appropriate for the operations they perform.
- What happens if a configured user has no role assigned? The system treats the user as having no permissions and rejects all requests with 403 Forbidden.
- What happens when there are repeated failed authentication attempts from the same source? The system logs each attempt (username, timestamp, IP) for operator review. Brute-force protection (rate limiting, lockout) is deferred to a future feature.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST require valid credentials for all endpoints except the health check endpoint.
- **FR-002**: System MUST support three distinct roles: APP, BACKOFFICE, and ADMIN.
- **FR-003**: The APP role MUST have access to all read-only operations (GET requests) on transactions, categories, and summary endpoints.
- **FR-004**: The APP role MUST NOT have access to any write operations (POST, PUT, DELETE) on transactions or categories.
- **FR-005**: The APP role MUST NOT have access to actuator endpoints (beyond health).
- **FR-006**: The BACKOFFICE role MUST have full CRUD access (GET, POST, PUT, DELETE) to all transaction and category endpoints.
- **FR-007**: The BACKOFFICE role MUST NOT have access to actuator endpoints (beyond health).
- **FR-008**: The ADMIN role MUST have full CRUD access to all transaction and category endpoints plus access to all actuator endpoints.
- **FR-009**: System MUST return a 401 Unauthorized response when no credentials or invalid credentials are provided.
- **FR-010**: System MUST return a 403 Forbidden response when a client has valid credentials but insufficient permissions for the requested operation.
- **FR-011**: The health check endpoint MUST remain accessible without credentials.
- **FR-012**: Credentials (usernames and passwords) MUST be configurable through application configuration, not hardcoded in source code.
- **FR-013**: Error responses for authentication and authorization failures MUST follow the existing error response format (`{status, error, message, details[]}`).
- **FR-014**: System MUST log all failed authentication attempts, capturing the attempted username, timestamp, and client IP address.

### Key Entities

- **User/Client**: A known consumer of the system, identified by a username and password, assigned exactly one role (APP, BACKOFFICE, or ADMIN).
- **Role**: A named permission level that determines which operations a client can perform. Three roles exist:
  - **APP**: Read-only access to transaction and category data.
  - **BACKOFFICE**: Full CRUD access to transactions and categories.
  - **ADMIN**: Full CRUD access plus actuator/system management access.

### Role Permission Matrix

| Operation                        | APP | BACKOFFICE | ADMIN |
| -------------------------------- | --- | ---------- | ----- |
| GET /v1/transactions             | Yes | Yes        | Yes   |
| GET /v1/transactions/{id}        | Yes | Yes        | Yes   |
| GET /v1/transactions/summary/*   | Yes | Yes        | Yes   |
| POST /v1/transactions            | No  | Yes        | Yes   |
| PUT /v1/transactions/{id}        | No  | Yes        | Yes   |
| DELETE /v1/transactions/{id}     | No  | Yes        | Yes   |
| GET /v1/categories               | Yes | Yes        | Yes   |
| GET /v1/categories/{id}          | Yes | Yes        | Yes   |
| POST /v1/categories              | No  | Yes        | Yes   |
| PUT /v1/categories/{id}          | No  | Yes        | Yes   |
| DELETE /v1/categories/{id}       | No  | Yes        | Yes   |
| GET /actuator/health             | N/A (public) | N/A (public) | N/A (public) |
| GET /actuator/info (and others)  | No  | No         | Yes   |

## Assumptions

- Authentication will use HTTP Basic Authentication, as the user explicitly requested simplicity and this is the simplest standard mechanism for securing a REST API.
- Users/clients are defined in application configuration rather than stored in a database. This keeps things simple and avoids the need for user management endpoints.
- The number of configured users will be small (under 10), so configuration-based user management is adequate.
- Passwords will be stored in hashed form in the configuration to avoid plaintext secrets.
- The existing error response format will be reused for authentication/authorization errors for consistency.
- The H2 console path (`/h2-console`) requires authentication (accessible to ADMIN only) to prevent unauthorized database access.
- Role names in configuration are case-insensitive (e.g., "admin", "ADMIN", "Admin" all map to the ADMIN role).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of non-health-check endpoints reject unauthenticated requests with a 401 response.
- **SC-002**: 100% of write operations (POST, PUT, DELETE) return 403 Forbidden when performed by an APP-role client.
- **SC-003**: 100% of actuator endpoints (beyond health) return 403 Forbidden when performed by APP or BACKOFFICE-role clients.
- **SC-004**: 100% of operations succeed when performed by a client with sufficient role permissions.
- **SC-005**: The health check endpoint responds successfully to unauthenticated requests 100% of the time.
- **SC-006**: All existing functionality continues to work correctly for authenticated users with appropriate roles (zero regression in existing features).
