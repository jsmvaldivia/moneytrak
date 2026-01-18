# MoneyTrak - Expense Tracking API

A Spring Boot REST API for tracking expenses with validation, optimistic locking, and comprehensive error handling.

## Features

- ✅ **Complete CRUD Operations** - Create, Read, Update, Delete expenses
- ✅ **Input Validation** - ISO 4217 currency codes, amount precision, date validation
- ✅ **Optimistic Locking** - Prevent lost updates with version-based conflict detection
- ✅ **Standardized Errors** - Consistent JSON error responses with field-level details
- ✅ **Comprehensive Tests** - 32 integration tests covering all endpoints
- ✅ **H2 Database** - File-based persistence with in-memory testing

## Quick Start

### Prerequisites

- Java 25
- Maven (or use included `mvnw` wrapper)

### Build and Run

```bash
# Build the project
./mvnw clean package

# Run tests
./mvnw test

# Start the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### H2 Console

Access the H2 database console at `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:file:./data/moneytrak`
- **Username**: `sa`
- **Password**: *(leave empty)*

## API Endpoints

### Create Expense
```bash
POST /v1/expenses
Content-Type: application/json

{
  "description": "Coffee at Starbucks",
  "amount": 4.50,
  "currency": "USD",
  "date": "2026-01-18T10:30:00Z"
}

Response: 201 Created
Location: /v1/expenses/{id}
```

### List All Expenses
```bash
GET /v1/expenses

Response: 200 OK
[
  {
    "id": "uuid",
    "description": "Coffee at Starbucks",
    "amount": 4.50,
    "currency": "USD",
    "date": "2026-01-18T10:30:00Z",
    "version": 0,
    "createdAt": "2026-01-18T10:30:00Z",
    "updatedAt": "2026-01-18T10:30:00Z"
  }
]
```

### Get Single Expense
```bash
GET /v1/expenses/{id}

Response: 200 OK
```

### Update Expense
```bash
PUT /v1/expenses/{id}
Content-Type: application/json

{
  "description": "Coffee at Starbucks (updated)",
  "amount": 5.00,
  "currency": "USD",
  "date": "2026-01-18T10:30:00Z",
  "version": 0
}

Response: 200 OK
{
  "id": "uuid",
  "description": "Coffee at Starbucks (updated)",
  "amount": 5.00,
  "currency": "USD",
  "date": "2026-01-18T10:30:00Z",
  "version": 1,  // Version incremented
  "createdAt": "2026-01-18T10:30:00Z",
  "updatedAt": "2026-01-18T10:35:00Z"
}
```

### Delete Expense
```bash
DELETE /v1/expenses/{id}

Response: 204 No Content
```

## Validation Rules

- **Description**: Required, max 500 characters
- **Amount**: Required, minimum 0.01, max 999,999,999.99 (2 decimal places)
- **Currency**: Required, valid ISO 4217 code (e.g., USD, EUR, GBP)
- **Date**: Required, must be past or present (no future dates)
- **Version**: Required for updates (optimistic locking)

## Error Responses

All errors follow this format:

```json
{
  "status": 400,
  "error": "ValidationError",
  "message": "Invalid expense data",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be greater than or equal to 0.01"
    }
  ]
}
```

### HTTP Status Codes

- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Validation errors
- `404 Not Found` - Resource not found
- `409 Conflict` - Optimistic lock version mismatch
- `500 Internal Server Error` - Unexpected errors

## Optimistic Locking

To prevent lost updates, the API uses version-based optimistic locking:

1. **Create** - Returns expense with `version: 0`
2. **Update** - Must include current `version` in request body
3. **Success** - Version increments to `1`
4. **Conflict** - If another update occurred, returns `409 Conflict`

Example conflict scenario:
```bash
# User A fetches expense (version: 0)
# User B fetches expense (version: 0)
# User A updates successfully (version: 0 → 1)
# User B tries to update with version: 0
# → Returns 409 Conflict: "Version mismatch: expense has been modified"
# User B must fetch latest version and retry
```

## Architecture

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 25
- **Database**: H2 (file-based)
- **ORM**: Hibernate 7.2.0
- **Validation**: Jakarta Bean Validation
- **Testing**: Spring Boot Test, MockMvc

### Project Structure

```
src/main/java/dev/juanvaldivia/moneytrak/expenses/
├── ExpenseController.java          # REST endpoints
├── ExpenseService.java              # Service interface
├── LocalExpenseService.java         # Service implementation
├── ExpenseRepository.java           # Spring Data JPA repository
├── Expense.java                     # JPA entity
├── dto/
│   ├── ExpenseCreationDto.java      # POST request DTO
│   ├── ExpenseUpdateDto.java        # PUT request DTO
│   ├── ExpenseDto.java              # Response DTO
│   ├── ErrorResponseDto.java        # Error response format
│   └── FieldErrorDto.java           # Field validation errors
├── mapper/
│   └── ExpenseMapper.java           # DTO ↔ Entity mapping
├── validation/
│   ├── ValidCurrency.java           # Currency validation annotation
│   └── CurrencyValidator.java       # Currency validator
└── exception/
    ├── GlobalExceptionHandler.java  # Global error handling
    ├── NotFoundException.java
    └── ConflictException.java
```

## Development

### Run Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=ExpenseControllerIntegrationTest
```

### Build Without Tests
```bash
./mvnw clean package -DskipTests
```

## License

This project is licensed under the terms specified in the LICENSE file.
