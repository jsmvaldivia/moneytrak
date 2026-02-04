# Data Model: Simple Role-Based Authentication

**Feature Branch**: `003-simple-auth`
**Date**: 2026-02-03

## Overview

This feature does **not** introduce new database entities. Users are defined in application configuration, not persisted to the database. The data model consists of configuration-bound objects and Spring Security's in-memory `UserDetails` representations.

## Entities

### ConfigUser (Configuration-Bound)

Represents a user defined in `application.yaml`, bound via `@ConfigurationProperties`.

| Field    | Type   | Constraints                                    | Description                    |
| -------- | ------ | ---------------------------------------------- | ------------------------------ |
| username | String | Required, non-blank                            | Unique identifier for the user |
| password | String | Required, non-blank, BCrypt-hashed with prefix | Encoded password               |
| role     | String | Required, one of: APP, BACKOFFICE, ADMIN       | User's permission level        |

**Notes**:
- Passwords stored with `{bcrypt}` prefix for `DelegatingPasswordEncoder` compatibility.
- Role is case-insensitive in configuration but mapped to uppercase internally.
- No database table — purely configuration-driven.

### Role (Enum)

Defines the three permission levels in the system.

| Value      | Description                                                   |
| ---------- | ------------------------------------------------------------- |
| APP        | Read-only access to transactions and categories               |
| BACKOFFICE | Full CRUD access to transactions and categories               |
| ADMIN      | Full CRUD access + actuator endpoints + H2 console access     |

**Hierarchy**: ADMIN > BACKOFFICE > APP (each higher role includes all lower-role permissions for `/v1/**` endpoints).

### Role Permission Mapping

| HTTP Method | /v1/** endpoints | /actuator/health | /actuator/* (other) | /h2-console |
| ----------- | ---------------- | ---------------- | ------------------- | ----------- |
| GET         | APP+             | Public           | ADMIN only          | ADMIN only  |
| POST        | BACKOFFICE+      | N/A              | N/A                 | N/A         |
| PUT         | BACKOFFICE+      | N/A              | N/A                 | N/A         |
| DELETE      | BACKOFFICE+      | N/A              | N/A                 | N/A         |

*"APP+" means APP, BACKOFFICE, and ADMIN. "BACKOFFICE+" means BACKOFFICE and ADMIN.*

## Configuration Schema

```yaml
moneytrak:
  security:
    users:
      - username: <string>
        password: <string>    # {bcrypt}$2a$10$...
        role: <APP|BACKOFFICE|ADMIN>
```

## Relationships to Existing Entities

- No foreign keys added to `transactions` or `categories` tables.
- No database migration required (no V3 migration).
- Authentication is stateless — no session table needed.
- User identity is not associated with individual transactions (out of scope for this feature).

## State Transitions

N/A — no entity lifecycle management. Users are static configuration.
