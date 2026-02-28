# Feature Specification: Portfolio Readings Management

**Feature Branch**: `004-portfolio-readings`
**Created**: 2026-02-28
**Status**: Draft
**Input**: User description: "Create specification for portfolio readings feature with accounts and readings management endpoints"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Account Management (Priority: P1)

Users need to define and manage their financial accounts (banks, brokers, investment platforms) where they hold assets. This establishes the foundation for tracking portfolio snapshots.

**Why this priority**: Without accounts, users cannot create readings. This is the foundational entity that enables all other functionality.

**Independent Test**: Can be fully tested by creating, updating, retrieving, and deleting accounts through the API. Delivers value by allowing users to catalog their financial accounts in one place.

**Acceptance Scenarios**:

1. **Given** no accounts exist, **When** user creates an account with name "Chase Savings", type "Bank", and currency "USD", **Then** system returns 201 with Location header pointing to the new account resource
2. **Given** user has created 3 accounts, **When** user requests all accounts, **Then** system returns 200 with list of 3 accounts ordered alphabetically by name (then by ID for duplicates)
3. **Given** an account exists with ID "123", **When** user retrieves account by ID, **Then** system returns 200 with account details including name, type, currency, and creation date
4. **Given** an account exists with name "TD Ameritrade" and version 0, **When** user updates the account name to "Schwab Brokerage", **Then** system returns 200 with updated account details and version 1
5. **Given** two users retrieve account with version 0, **When** both users update the account concurrently, **Then** first update succeeds with version 1, second update receives 409 Conflict with message "Account was modified by another user"
6. **Given** an account exists with no associated readings, **When** user deletes the account, **Then** system returns 204 and account is permanently removed
7. **Given** user attempts to create an account with invalid currency "XYZ", **When** request is submitted, **Then** system returns 400 with validation error details
8. **Given** user has 1000 accounts, **When** user attempts to create account 1001, **Then** system returns 409 Conflict with message "Account limit of 1000 reached"

---

### User Story 2 - Portfolio Snapshot Recording (Priority: P2)

Users need to record point-in-time snapshots of their account balances to track portfolio value changes over time. Each reading captures the balance of a specific account at a specific date.

**Why this priority**: This is the core value proposition - capturing portfolio history. Depends on accounts existing first (P1).

**Independent Test**: Can be tested by creating readings for existing accounts, retrieving them, and verifying date/amount accuracy. Delivers value by enabling historical portfolio tracking.

**Acceptance Scenarios**:

1. **Given** an account exists with ID "123", **When** user creates a reading with accountId "123", amount 15000.50, and date "2026-02-27T10:00:00Z", **Then** system returns 201 with Location header and reading details
2. **Given** a reading exists with ID "456", **When** user retrieves reading by ID, **Then** system returns 200 with reading details including amount, date, and associated account information (name, type, currency)
3. **Given** user attempts to create a reading with a future date, **When** request is submitted, **Then** system returns 400 with validation error "Reading date cannot be in the future"
4. **Given** user attempts to create a reading for non-existent account ID "999", **When** request is submitted, **Then** system returns 404 with error "Account not found"
5. **Given** a reading exists with amount 15000.50 and version 0, **When** user updates the amount to 15500.75, **Then** system returns 200 with updated reading details and version 1
6. **Given** two users retrieve reading with version 0, **When** both users update the reading concurrently, **Then** first update succeeds with version 1, second update receives 409 Conflict with message "Reading was modified by another user"
7. **Given** user attempts to create a reading with negative amount -500.00 for margin account, **When** request is submitted, **Then** system accepts the reading (negative amounts allowed for debts/margins)
8. **Given** user attempts to create a reading with 8 decimal places for cryptocurrency (0.12345678 BTC), **When** request is submitted, **Then** system accepts the reading with full precision

---

### User Story 3 - Latest Portfolio View (Priority: P3)

Users need to quickly view their current portfolio status by retrieving the most recent reading for each account. This provides a consolidated snapshot of their current financial position.

**Why this priority**: Builds on P1 and P2 to provide a summary view. Most valuable when historical data already exists.

**Independent Test**: Can be tested by creating multiple readings with different dates and verifying the endpoint returns only the latest reading per account. Delivers value by providing instant portfolio overview.

**Acceptance Scenarios**:

1. **Given** account "123" has readings dated 2026-02-01, 2026-02-15, and 2026-02-27, **When** user requests latest readings, **Then** system returns only the 2026-02-27 reading for account "123"
2. **Given** three accounts exist with readings, **When** user requests latest readings, **Then** system returns one reading per account (the most recent for each)
3. **Given** no readings exist for any account, **When** user requests latest readings, **Then** system returns 200 with empty array
4. **Given** an account has no readings but other accounts do, **When** user requests latest readings, **Then** system returns readings only for accounts with data

---

### User Story 4 - Reading History Management (Priority: P4)

Users need to soft-delete incorrect or duplicate readings while preserving historical data integrity. Soft deletion allows recovery if needed and maintains audit trails.

**Why this priority**: Error correction capability. Important but not blocking for core functionality.

**Independent Test**: Can be tested by soft-deleting readings and verifying they no longer appear in latest readings but remain in the database. Delivers value by allowing users to correct mistakes without data loss.

**Acceptance Scenarios**:

1. **Given** a reading exists with ID "789", **When** user deletes the reading, **Then** system returns 204 and reading is marked as deleted (not physically removed)
2. **Given** a reading is soft-deleted, **When** user requests latest readings, **Then** deleted reading does not appear in results
3. **Given** a reading is soft-deleted, **When** user attempts to update it, **Then** system returns 404 with error "Reading not found"
4. **Given** an account has only soft-deleted readings, **When** user requests latest readings, **Then** no reading is returned for that account

---

### User Story 5 - Account Deletion Constraints (Priority: P5)

Users need to understand and handle account deletion rules. Accounts with active readings cannot be removed to maintain referential integrity and historical accuracy. Accounts with only soft-deleted readings can be removed (user has already cleaned up their data).

**Why this priority**: Data integrity safeguard. Prevents accidental data loss. Lower priority as it's a constraint rather than core feature.

**Independent Test**: Can be tested by attempting to delete accounts with and without readings, verifying appropriate responses. Delivers value by protecting historical data.

**Acceptance Scenarios**:

1. **Given** an account has 5 active readings, **When** user attempts to delete the account, **Then** system returns 409 with error "Cannot delete account with 5 active readings. Delete or archive readings first."
2. **Given** an account has only soft-deleted readings (user has cleaned up all data), **When** user attempts to delete the account, **Then** system returns 204 and account is permanently removed (soft-deleted readings do not prevent deletion)
3. **Given** an account has 3 active readings and 2 soft-deleted readings, **When** user attempts to delete the account, **Then** system returns 409 with count of active readings only
4. **Given** an account has no readings, **When** user deletes the account, **Then** system returns 204 and account is permanently removed

---

### User Story 6 - Account Reading History (Priority: P3)

Users need to view the complete history of readings for a specific account to track value changes over time. This enables trend analysis and verification of entered data.

**Why this priority**: Enables the core value proposition of tracking portfolio changes over time. Without this, users can only see current snapshots (latest readings) but cannot analyze trends or verify historical data entry.

**Independent Test**: Can be tested by creating multiple readings for one account and verifying the endpoint returns all readings in chronological order. Delivers value by enabling historical trend analysis.

**Acceptance Scenarios**:

1. **Given** account "123" has 10 readings spanning 6 months, **When** user requests GET /v1/accounts/123/readings, **Then** system returns all 10 readings ordered by readingDate descending (most recent first)
2. **Given** account "123" has 5 active readings and 2 soft-deleted readings, **When** user requests account reading history, **Then** system returns only the 5 active readings (soft-deleted excluded)
3. **Given** account "123" has no readings, **When** user requests account reading history, **Then** system returns 200 with empty array
4. **Given** user requests reading history for non-existent account ID "999", **When** request is submitted, **Then** system returns 404 with error "Account not found"
5. **Given** account "123" has readings with same amounts but different dates, **When** user requests reading history, **Then** system returns all readings in chronological order allowing user to see duplicate snapshots

---

### Edge Cases

**Resolved edge case decisions**:

1. **Duplicate readings same account/date**: System **allows** multiple readings for the same account on the same date/time. No uniqueness constraint enforced. Users may want to track intraday portfolio changes (morning/evening snapshots).

2. **Zero and negative amounts**: System **allows** zero amounts (empty accounts) and **negative amounts** (debts, margin accounts, credit card balances). No minimum value validation beyond data type constraints.

3. **Updating reading's accountId**: AccountId is **immutable** after creation. Readings cannot be moved between accounts. If wrong account was selected, user must delete and recreate the reading.

4. **Large decimal precision (crypto)**: System supports amounts with **up to 8 decimal places** (e.g., 0.12345678 BTC). Precision validation: 15 total digits, 8 fractional digits. Supports most cryptocurrency precision requirements.

5. **Thousands of accounts performance**: System enforces a **hard limit of 1000 accounts per user** (no pagination on latest readings endpoint). Attempting to create account #1001 returns 409 Conflict. This limit may be increased in future versions with pagination support.

6. **Soft deletion + optimistic locking**: Soft-deleted readings are **read-only tombstones**. Once soft-deleted, readings cannot be updated or permanently deleted in v1. Future versions may add hard-delete capability for ADMIN role.

## Requirements *(mandatory)*

### Functional Requirements

**Account Management:**

- **FR-001**: System MUST allow users to create accounts with name (required, max 100 characters), type (required, enum), and currency (required, ISO 4217 code)
- **FR-002**: System MUST support account types: BANK, BROKER, STOCK, P2P, CRYPTO, and OTHER for future extensibility
- **FR-003**: System MUST validate currency codes against ISO 4217 standard using existing @ValidCurrency annotation
- **FR-004**: System MUST generate UUID identifiers for accounts (consistent with existing entities)
- **FR-005**: System MUST prevent deletion of accounts that have active readings (soft-deleted readings do not prevent deletion)
- **FR-006**: System MUST return 409 Conflict with active reading count when attempting to delete an account with active readings (e.g., "Cannot delete account with 5 active readings")
- **FR-007**: System MUST allow updating account name, type, and currency for existing accounts
- **FR-008**: System MUST support optimistic locking for account updates using @Version field
- **FR-009**: System MUST return 404 Not Found when retrieving, updating, or deleting a non-existent account
- **FR-009A**: System MUST provide endpoint to list all accounts ordered by name (alphabetically), then by ID as tiebreaker for deterministic results
- **FR-009B**: System MUST enforce a hard limit of 1000 accounts per user, returning 409 Conflict when limit is reached

**Portfolio Reading Management:**

- **FR-010**: System MUST allow users to create readings with accountId (required), amount (required), and readingDate (required)
- **FR-011**: System MUST validate that accountId references an existing, non-deleted account (404 if not found)
- **FR-012**: System MUST validate amounts using BigDecimal with precision of 15 digits total, 8 decimal places maximum (supporting cryptocurrency precision like Bitcoin/Ethereum)
- **FR-012A**: System MUST allow zero and negative amounts (for empty accounts, debts, margin accounts, credit card balances)
- **FR-012B**: System MUST provide endpoint to retrieve a single reading by ID, including associated account details
- **FR-013**: System MUST validate reading dates are not in the future using @PastOrPresent annotation
- **FR-014**: System MUST store reading dates as ZonedDateTime in UTC timezone (consistent with transactions)
- **FR-015**: System MUST support optimistic locking for reading updates using @Version field
- **FR-016**: System MUST implement soft deletion for readings (mark as deleted, not physically remove)
- **FR-017**: System MUST exclude soft-deleted readings from all query results (latest readings, by ID)
- **FR-018**: System MUST return 404 when attempting to retrieve or update a soft-deleted reading
- **FR-019**: System MUST allow updating amount and readingDate for existing readings (accountId remains immutable after creation)
- **FR-020**: System MUST return 204 No Content after successful soft deletion

**Latest Readings Query:**

- **FR-021**: System MUST provide endpoint to retrieve the most recent reading for each account (one per account)
- **FR-022**: System MUST determine "latest" based on readingDate (most recent date/time)
- **FR-023**: System MUST exclude soft-deleted readings from latest readings calculation
- **FR-024**: System MUST exclude accounts with no active readings from latest readings results
- **FR-025**: System MUST return readings ordered by account name (alphabetically), then by account ID as tiebreaker for deterministic results
- **FR-026**: System MUST include account details (name, type, currency) in each reading response for convenience
- **FR-026A**: System MUST provide endpoint to retrieve all readings for a specific account, ordered by readingDate descending (most recent first)
- **FR-026B**: System MUST exclude soft-deleted readings from account reading history results

**API Consistency:**

- **FR-027**: All POST endpoints MUST return 201 Created with Location header pointing to the created resource
- **FR-028**: All endpoints MUST follow existing validation patterns (@Valid, @NotNull, @NotBlank)
- **FR-029**: All endpoints MUST use /v1/ versioning prefix (e.g., /v1/accounts, /v1/readings)
- **FR-030**: All error responses MUST use existing ErrorResponseDto format (status, error, message, details[])
- **FR-031**: All DTOs MUST follow existing patterns (CreationDto, UpdateDto, ResponseDto with records)
- **FR-032**: All update DTOs MUST include version field for optimistic locking

### Key Entities

**Account**: Represents a financial account where assets are held
- **Attributes**: id (UUID), name (string, max 100 chars), type (enum: BANK, BROKER, STOCK, P2P, CRYPTO, OTHER), currency (ISO 4217 code), createdAt (ZonedDateTime), updatedAt (ZonedDateTime), version (Long for optimistic locking)
- **Relationships**: One account can have many readings (one-to-many, unidirectional from Reading to Account)
- **Validation**: Name required, type required, currency must be valid ISO 4217 code
- **Constraints**: System enforces maximum 1000 accounts per user
- **Deletion**: Hard delete allowed only if no active readings exist (soft-deleted readings do not prevent deletion)

**Reading**: Represents a point-in-time snapshot of an account balance
- **Attributes**: id (UUID), accountId (UUID, reference to Account), amount (BigDecimal, 15 digits total, up to 8 decimal places), readingDate (ZonedDateTime in UTC), soft deletion marker, createdAt (ZonedDateTime), updatedAt (ZonedDateTime), version (Long for optimistic locking)
- **Relationships**: Many-to-one with Account (unidirectional reference)
- **Validation**: AccountId must reference existing account, amount can be positive, zero, or negative (for debts/margins), readingDate cannot be future
- **Immutability**: AccountId cannot be changed after creation (readings cannot move between accounts)
- **Deletion**: Soft delete only (readings are marked as deleted and excluded from queries, but data is preserved)

### Security Requirements

**Authentication Method**: HTTP Basic (default)

**Role-Based Access Control**:

| Endpoint Pattern | HTTP Methods | Required Roles | Rationale |
|------------------|-------------|----------------|-----------|
| `GET /v1/accounts/*` | GET | APP, BACKOFFICE, ADMIN | Read access for all authenticated users |
| `POST /v1/accounts` | POST | BACKOFFICE, ADMIN | Write operations require elevated privileges |
| `PUT /v1/accounts/*` | PUT | BACKOFFICE, ADMIN | Update operations require elevated privileges |
| `DELETE /v1/accounts/*` | DELETE | BACKOFFICE, ADMIN | Delete operations require elevated privileges |
| `GET /v1/readings/*` | GET | APP, BACKOFFICE, ADMIN | Read access for all authenticated users |
| `POST /v1/readings` | POST | BACKOFFICE, ADMIN | Write operations require elevated privileges |
| `PUT /v1/readings/*` | PUT | BACKOFFICE, ADMIN | Update operations require elevated privileges |
| `DELETE /v1/readings/*` | DELETE | BACKOFFICE, ADMIN | Delete operations require elevated privileges |

**Public Endpoints**: None (all endpoints require authentication, consistent with existing /v1/** behavior)

**Error Responses**:
- **401 Unauthorized**: Missing or invalid credentials → `ErrorResponseDto` format
- **403 Forbidden**: Valid credentials but insufficient role permissions → `ErrorResponseDto` format

**Security Test Coverage Required**:
- **AUTH-001**: Unauthenticated requests to /v1/accounts/** return 401
- **AUTH-002**: Unauthenticated requests to /v1/readings/** return 401
- **AUTH-003**: APP role can access GET /v1/accounts/** and GET /v1/readings/**
- **AUTH-004**: APP role receives 403 for POST/PUT/DELETE on /v1/accounts/** and /v1/readings/**
- **AUTH-005**: BACKOFFICE role can access all CRUD operations on /v1/accounts/** and /v1/readings/**
- **AUTH-006**: ADMIN role can access all operations on /v1/accounts/** and /v1/readings/**
- **AUTH-007**: Invalid credentials return 401 with failed auth logging

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create and manage up to 50 accounts without performance degradation (response time under 200ms)
- **SC-002**: Users can record portfolio readings in under 30 seconds per account
- **SC-003**: Latest readings endpoint returns results for 50 accounts in under 500ms (must avoid N+1 queries through JOINs or batch fetching when including account details)
- **SC-004**: System supports storing 10,000+ historical readings without query performance degradation
- **SC-005**: 95% of account and reading creation requests succeed on first attempt without validation errors
- **SC-006**: Users can view their current portfolio snapshot (latest readings) in a single API call without client-side aggregation
- **SC-007**: Soft-deleted readings are excluded from all queries with 100% consistency
- **SC-008**: Account deletion constraints prevent data loss in 100% of cases (no orphaned readings)

### User Experience Goals

- **SC-009**: API responses follow consistent patterns with existing MoneyTrak endpoints (same error formats, status codes, validation messages)
- **SC-010**: Users can understand their portfolio structure by querying accounts and latest readings with clear, self-documenting responses

## Assumptions *(optional)*

- Account types are predefined enums (BANK, BROKER, STOCK, P2P, CRYPTO, OTHER) and not user-customizable
- Each reading represents the total balance/value of an account at a point in time (not individual transactions or holdings)
- Users manually enter readings (no automatic imports from financial institutions in this phase)
- Readings can have duplicate dates for the same account (users might take multiple snapshots per day) - no uniqueness constraint
- Amount values can be zero (empty account), positive, or negative (for debts, margin accounts, credit card balances)
- Amounts support up to 8 decimal places for cryptocurrency precision (e.g., Bitcoin, Ethereum)
- Currency is stored at account level only (readings inherit currency from their account, no multi-currency support per reading)
- Soft deletion is permanent (no restore endpoint in v1) but preserves data for potential future restoration
- Soft-deleted readings become read-only tombstones (cannot be updated or hard-deleted in v1)
- System enforces hard limit of 1000 accounts per user (no pagination on latest readings endpoint)
- Account updates (name, type, currency) apply to all past readings retroactively (readings reflect current account properties, not historical snapshots at time of creation)
- AccountId in readings is immutable after creation (readings cannot move between accounts)

## Dependencies *(optional)*

### Internal Dependencies
- Existing Spring Boot 4.0.1 infrastructure (Spring Web, Spring Data JPA, Hibernate ORM 7.2.0)
- Existing validation infrastructure (@ValidCurrency annotation, GlobalExceptionHandler)
- Existing security infrastructure (SecurityConfig, role-based access control)
- Existing database migration system (Flyway for schema changes)
- Existing DTO patterns (CreationDto, UpdateDto, ResponseDto with records)
- Existing optimistic locking patterns (@Version field with EntityManager.flush())

### External Dependencies
- H2 database for persistence (file-based for production, in-memory for tests)
- ISO 4217 currency code validation

### Performance Dependencies
- **Database indexes required** for query performance with 10,000+ readings:
  - Reading table: Composite index on (accountId, readingDate DESC) for efficient latest readings queries
  - Reading table: Index on deleted flag for soft-delete filtering (if supported by database)
  - Account table: Index on name for alphabetical ordering
- Query optimization: Latest readings and account reading history endpoints must use JOINs or batch fetching to avoid N+1 query problems

### Migration Considerations
- No data migration required (new feature, no existing data)
- New database tables will be created via Flyway migrations (V3__create_accounts_and_readings.sql)
- No impact on existing transactions or categories features

## Out of Scope *(optional)*

The following are explicitly NOT part of this feature:

- **Automatic data imports**: No integration with bank APIs, Plaid, or financial data providers
- **Historical data aggregation**: No automatic calculation of portfolio totals, growth rates, or performance metrics
- **Multi-currency conversion**: No automatic exchange rate lookups or portfolio value normalization to a single currency
- **Reading validation against previous readings**: No checks for suspiciously large balance changes
- **Account hierarchies or grouping**: No parent/child account relationships or custom grouping
- **Filtering readings by date range**: Only latest readings endpoint provided (date range queries could be added later)
- **Bulk import/export**: No CSV import or data export functionality
- **Reading categories or tags**: Readings are simple snapshots without additional classification
- **Account-level notes or metadata**: Accounts have only name, type, and currency
- **Restore functionality for soft-deleted readings**: Soft deletion is permanent in v1 (no un-delete endpoint)
- **Soft-delete cleanup/archival**: No automatic cleanup or hard-delete capability for soft-deleted readings in v1 (may require backup/restore strategy in future version to prevent database bloat)
- **Account balance calculations**: No automatic balance calculation from transactions (readings are manual, independent snapshots)
- **Pagination on latest readings or account reading history**: Hard limit of 1000 accounts enforced instead of pagination
