# Quickstart Guide: Expense Tracking API

**Feature**: 001-expense-tracking
**Last Updated**: 2026-01-16

This guide helps you set up, develop, and test the expense tracking API using Test-Driven Development.

## Prerequisites

- Java 25 installed (`java --version` should show 25.x)
- Maven wrapper already configured in project (`./mvnw`)
- IDE with Java support (IntelliJ IDEA, VS Code with Java extensions, Eclipse)

## Project Setup

### 1. Add Dependencies to pom.xml

Add these dependencies to your existing `pom.xml`:

```xml
<!-- JPA and Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

The project already has:
- `spring-boot-starter-web` (REST APIs)
- `spring-boot-starter-validation` (Jakarta Validation)
- `spring-boot-starter-test` (Testing)

### 2. Configure Application

Update `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: moneytrak

  datasource:
    url: jdbc:h2:file:./data/moneytrak
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true  # Access at http://localhost:8080/h2-console

  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update schema
    show-sql: false    # Set true for SQL debugging
    properties:
      hibernate:
        format_sql: true
```

Create `src/test/resources/application-test.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # In-memory for fast tests
  jpa:
    hibernate:
      ddl-auto: create-drop  # Clean database per test
```

### 3. Build and Verify Setup

```bash
# Clean build
./mvnw clean package

# Run existing tests (should pass)
./mvnw test

# Start application (should start without errors)
./mvnw spring-boot:run
```

## TDD Workflow

Following Constitution Gate 3 (Test-Driven Development - NON-NEGOTIABLE):

### Red-Green-Refactor Cycle

1. **RED**: Write a failing test for one requirement
2. **Verify RED**: Run test, confirm it fails
3. **GREEN**: Write minimal code to pass the test
4. **Verify GREEN**: Run test, confirm it passes
5. **REFACTOR**: Clean up code (if needed)
6. **Repeat**: Next requirement

### Recommended Implementation Order

Follow spec.md user story priorities:

#### Phase 1: P1 - Record Single Expense (User Story 1)

**Iteration 1: Entity & Repository**

1. Write test: `ExpenseRepositoryTest.testSaveExpense()`
2. Create `Expense` entity with JPA annotations
3. Create `ExpenseRepository` interface
4. Run test → GREEN

**Iteration 2: DTO & Mapper**

1. Write test: `ExpenseMapperTest.testToEntity()`
2. Create `ExpenseCreationDto` with validation
3. Create `ExpenseMapper.toEntity()`
4. Run test → GREEN

**Iteration 3: Service Layer**

1. Write test: `ExpenseServiceTest.testCreateExpense()`
2. Create `ExpenseService.createExpense()`
3. Run test → GREEN

**Iteration 4: Controller**

1. Write test: `ExpenseControllerTest.testCreateExpense()` (MockMvc)
2. Create `ExpenseController.createExpense()`
3. Run test → GREEN

**Iteration 5: Validation**

1. Write test: `ExpenseControllerTest.testCreateExpense_InvalidData()`
2. Add `@Valid` annotation
3. Run test → GREEN

**Iteration 6: Integration**

1. Write test: `ExpenseControllerIntegrationTest.testCreateExpenseE2E()`
2. Run test → GREEN (should pass if all units work)

#### Phase 2: P2 - Retrieve Recorded Expenses (User Story 2)

**List All**: Similar TDD cycle for GET /v1/expenses
**Get Single**: Similar TDD cycle for GET /v1/expenses/{id}

#### Phase 3: P3 - Update & Delete (User Stories 3 & 4)

**Update**: TDD for PUT /v1/expenses/{id} with optimistic locking
**Delete**: TDD for DELETE /v1/expenses/{id}

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ExpenseControllerTest

# Run specific test method
./mvnw test -Dtest=ExpenseControllerTest#testCreateExpense

# Run with coverage (if JaCoCo configured)
./mvnw clean test jacoco:report
```

## Manual Testing with curl

### Create Expense

```bash
curl -X POST http://localhost:8080/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Coffee at Starbucks",
    "amount": 4.50,
    "currency": "USD",
    "date": "2026-01-16T09:30:00-05:00"
  }'
```

Expected response (201 Created):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "description": "Coffee at Starbucks",
  "amount": 4.50,
  "currency": "USD",
  "date": "2026-01-16T14:30:00Z",
  "version": 1,
  "createdAt": "2026-01-16T14:30:00Z",
  "updatedAt": "2026-01-16T14:30:00Z"
}
```

### List Expenses

```bash
curl http://localhost:8080/v1/expenses
```

### Get Single Expense

```bash
curl http://localhost:8080/v1/expenses/123e4567-e89b-12d3-a456-426614174000
```

### Update Expense

```bash
curl -X PUT http://localhost:8080/v1/expenses/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Coffee (updated)",
    "version": 1
  }'
```

Expected response (200 OK) with incremented version:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "description": "Coffee (updated)",
  "version": 2,
  ...
}
```

### Delete Expense

```bash
curl -X DELETE http://localhost:8080/v1/expenses/123e4567-e89b-12d3-a456-426614174000
```

Expected response: 204 No Content

### Test Validation Errors

```bash
# Future date (should return 400)
curl -X POST http://localhost:8080/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": 10.00,
    "currency": "USD",
    "date": "2030-01-01T00:00:00Z"
  }'

# Invalid currency (should return 400)
curl -X POST http://localhost:8080/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": 10.00,
    "currency": "XXX",
    "date": "2026-01-16T00:00:00Z"
  }'

# Negative amount (should return 400)
curl -X POST http://localhost:8080/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": -5.00,
    "currency": "USD",
    "date": "2026-01-16T00:00:00Z"
  }'
```

### Test Optimistic Locking

```bash
# 1. Create expense
RESPONSE=$(curl -s -X POST http://localhost:8080/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": 10.00,
    "currency": "USD",
    "date": "2026-01-16T00:00:00Z"
  }')

ID=$(echo $RESPONSE | jq -r '.id')

# 2. Update with correct version (should succeed)
curl -X PUT http://localhost:8080/v1/expenses/$ID \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated once",
    "version": 1
  }'

# 3. Try to update with old version (should return 409 Conflict)
curl -X PUT http://localhost:8080/v1/expenses/$ID \
  -H "Content-Type: application/json" \
  -d '{
    "description": "This should fail",
    "version": 1
  }'
```

## Debugging

### Access H2 Console

1. Start application: `./mvnw spring-boot:run`
2. Open browser: http://localhost:8080/h2-console
3. Connection details:
   - JDBC URL: `jdbc:h2:file:./data/moneytrak`
   - User: `sa`
   - Password: (leave empty)
4. Click "Connect"
5. Run SQL queries to inspect database

### Enable SQL Logging

In `application.yaml`:
```yaml
spring:
  jpa:
    show-sql: true  # Print SQL statements
```

### Common Issues

**Build fails with "package jakarta.persistence does not exist"**
- Solution: Ensure `spring-boot-starter-data-jpa` is in `pom.xml`
- Run: `./mvnw clean install`

**Tests fail with database connection errors**
- Solution: Check `application-test.yaml` uses `jdbc:h2:mem:testdb`
- Ensure `@SpringBootTest` tests use `@ActiveProfiles("test")`

**Validation not working (no errors for invalid data)**
- Solution: Add `@Valid` annotation on controller method parameters
- Ensure `spring-boot-starter-validation` is in dependencies

**Optimistic locking not throwing 409**
- Solution: Verify `@Version` annotation on `Expense.version` field
- Check DTO includes version field in update requests

## Next Steps

After completing implementation:

1. Run full test suite: `./mvnw test`
2. Verify test coverage meets targets (80% service layer, 100% validation)
3. Generate OpenAPI docs (consider adding `springdoc-openapi-starter-webmvc-ui`)
4. Run integration tests against real database
5. Prepare for production: Switch to PostgreSQL, add Flyway migrations
6. Create tasks with `/speckit.tasks` command

## Development Tips

1. **Keep tests focused**: One test per requirement/scenario
2. **Use descriptive test names**: `testCreateExpense_WhenAmountNegative_ReturnsValidationError`
3. **Test error paths**: Don't just test happy paths
4. **Mock external dependencies**: Use `@MockBean` for repositories in service tests
5. **Use `@WebMvcTest` for controller tests**: Faster than `@SpringBootTest`
6. **Clean up test data**: Use `@Transactional` on test methods for auto-rollback

## Reference Documentation

- **Spec**: [spec.md](spec.md) - Feature requirements
- **Research**: [research.md](research.md) - Technology decisions
- **Data Model**: [data-model.md](data-model.md) - Entity & DTO definitions
- **API Contract**: [contracts/openapi.yaml](contracts/openapi.yaml) - OpenAPI spec
- **Constitution**: [../../.specify/memory/constitution.md](../../.specify/memory/constitution.md) - Project principles