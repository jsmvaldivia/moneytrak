# Security Rules Contract: Simple Role-Based Authentication

**Feature Branch**: `003-simple-auth`
**Date**: 2026-02-03

## Overview

This feature adds HTTP Basic Authentication to all existing endpoints. No new API endpoints are introduced. The contract below defines the security behavior layered on top of existing endpoint contracts.

## Authentication

**Mechanism**: HTTP Basic Authentication
**Header**: `Authorization: Basic <base64(username:password)>`

### Unauthenticated Responses

All protected endpoints return 401 when credentials are missing or invalid.

**Response** (401 Unauthorized):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Provide valid credentials.",
  "details": []
}
```

**Response headers**: `WWW-Authenticate: Basic realm="MoneyTrak API"`

### Forbidden Responses

Authenticated users without sufficient permissions receive 403.

**Response** (403 Forbidden):
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Insufficient permissions for this operation.",
  "details": []
}
```

## Authorization Rules

### Public Endpoints (No Authentication Required)

| Endpoint             | Method | Description        |
| -------------------- | ------ | ------------------ |
| /actuator/health     | GET    | Health check       |

### APP Role (Read-Only)

| Endpoint                            | Method | Description               |
| ----------------------------------- | ------ | ------------------------- |
| /v1/transactions                    | GET    | List transactions         |
| /v1/transactions/{id}               | GET    | Get transaction           |
| /v1/transactions/summary/expenses   | GET    | Expense total             |
| /v1/transactions/summary/income     | GET    | Income total              |
| /v1/categories                      | GET    | List categories           |
| /v1/categories/{id}                 | GET    | Get category              |

### BACKOFFICE Role (Full CRUD)

*Includes all APP permissions plus:*

| Endpoint                    | Method | Description            |
| --------------------------- | ------ | ---------------------- |
| /v1/transactions            | POST   | Create transaction     |
| /v1/transactions/{id}       | PUT    | Update transaction     |
| /v1/transactions/{id}       | DELETE | Delete transaction     |
| /v1/categories              | POST   | Create category        |
| /v1/categories/{id}         | PUT    | Update category        |
| /v1/categories/{id}         | DELETE | Delete category        |

### ADMIN Role (Full Access)

*Includes all BACKOFFICE permissions plus:*

| Endpoint                    | Method | Description                |
| --------------------------- | ------ | -------------------------- |
| /actuator/info              | GET    | Application info           |
| /actuator/*                 | GET    | All actuator endpoints     |
| /h2-console/**              | ANY    | Database console access    |

## Logging Contract

### Failed Authentication Logging (FR-014)

Every failed authentication attempt MUST produce a log entry containing:

| Field     | Description                              | Example                    |
| --------- | ---------------------------------------- | -------------------------- |
| username  | The attempted username                   | `unknown-user`             |
| timestamp | ISO 8601 timestamp of the attempt        | `2026-02-03T14:30:00.000Z` |
| ip        | Client IP address from request           | `192.168.1.100`            |
| level     | Log level                                | `WARN`                     |

**Log format**: `WARN - Failed authentication attempt: username='{}', ip='{}', timestamp='{}'`

## Behavior Changes to Existing Endpoints

No functional changes to existing endpoint behavior. The only additions are:
1. All requests require valid HTTP Basic credentials (except health).
2. Write operations require BACKOFFICE or ADMIN role.
3. Read operations require any valid role.
4. Error responses for 401/403 follow the existing `ErrorResponseDto` format.
