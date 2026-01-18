# Research: Expense Tracking API

**Date**: 2026-01-16
**Feature**: 001-expense-tracking
**Phase**: 0 (Research & Decisions)

## Storage Solution

### Decision
Use **SQLite** with file-based persistence for production-ready local storage.

### Rationale
1. **Production-Ready**: Unlike H2 (primarily for testing), SQLite is battle-tested for production use
2. **Zero Infrastructure**: File-based database with no server setup required
3. **Spec Compliance**: FR-007 persistence requirement met with `jdbc:sqlite:./data/moneytrak.db`
4. **TDD-Friendly**: In-memory mode (`jdbc:sqlite::memory:`) for fast, isolated tests
5. **Migration Path**: If multi-user/cloud deployment needed later, can migrate to PostgreSQL or Turso (distributed SQLite)
6. **Constitution Alignment**: Simple, reliable, no premature optimization

### Alternatives Considered
- **H2**: Rejected - primarily a testing database, not recommended for production
- **PostgreSQL from start**: Rejected - adds deployment complexity before MVP validation
- **Turso (managed SQLite)**: Deferred - good for distributed/edge deployment but overkill for single-user MVP

### Implementation Details
- **Dependency**: `org.xerial:sqlite-jdbc` + `hibernate-community-dialects` (for SQLite dialect)
- **Configuration**:
    - Development/Production: `jdbc:sqlite:./data/moneytrak.db` (file-based)
    - Tests: `jdbc:sqlite::memory:` (in-memory, fast isolation)
- **Schema Management**: JPA/Hibernate auto-generate initially, migrate to Flyway for production

## Testing Strategy

### Decision
Follow **constitution's test pyramid** with 70% unit, 20% integration, 10% E2E.

### Test Structure
1. **Unit Tests (70%)**:
   - `ExpenseServiceTest`: Service logic with mocked repository
   - `ExpenseMapperTest`: DTO ↔ Domain conversion
   - `CurrencyValidatorTest`: Custom validator logic
   - `ExpenseValidationTest`: Validation annotation behavior

2. **Integration Tests (20%)**:
   - `ExpenseControllerIntegrationTest` with `@SpringBootTest`: Full request/response cycle with real database
   - `ExpenseRepositoryTest` with `@DataJpaTest`: JPA entity mappings and queries

3. **Contract Tests (10%)**:
   - `ExpenseControllerTest` with `@WebMvcTest`: HTTP contract validation (status codes, headers, JSON structure)

### TDD Workflow
Per constitution Gate 3:
1. Write failing test for requirement
2. Run test, verify RED
3. Implement minimal code to pass
4. Run test, verify GREEN
5. Refactor if needed
6. Repeat for next requirement

## Project Structure

### Decision
Standard **Spring Boot Maven project** with feature-based packages.

### Structure
```
src/
├── main/
│   ├── java/
│   │   └── dev/juanvaldivia/moneytrak/
│   │       ├── MoneyTrakApplication.java
│   │       └── expenses/              # Feature package
│   │           ├── Expense.java       # Domain entity
│   │           ├── ExpenseController.java
│   │           ├── ExpenseService.java
│   │           ├── ExpenseRepository.java (interface)
│   │           ├── dto/
│   │           │   ├── ExpenseCreationDto.java
│   │           │   ├── ExpenseUpdateDto.java
│   │           │   └── ExpenseDto.java
│   │           ├── mapper/
│   │           │   └── ExpenseMapper.java
│   │           └── validation/
│   │               ├── ValidCurrency.java
│   │               └── CurrencyValidator.java
│   └── resources/
│       └── application.yaml
│
└── test/
    └── java/
        └── dev/juanvaldivia/moneytrak/
            └── expenses/
                ├── ExpenseControllerTest.java         # @WebMvcTest
                ├── ExpenseControllerIntegrationTest.java  # @SpringBootTest
                ├── ExpenseServiceTest.java            # Unit
                ├── ExpenseRepositoryTest.java         # @DataJpaTest
                ├── ExpenseMapperTest.java             # Unit
                └── validation/
                    └── CurrencyValidatorTest.java     # Unit
```

**Rationale**: Constitution Gate 1 requires feature-based organization. All expense-related code lives in `expenses` package.

## Dependencies Summary

### Maven Dependencies to Add
```xml
<!-- Already in pom.xml from existing project -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- New dependencies needed -->
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

## Configuration

### application.yaml
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
      enabled: true  # Enable H2 console at /h2-console for debugging

  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update schema (change to 'validate' in prod)
    show-sql: false    # Set to true for debugging
    properties:
      hibernate:
        format_sql: true
```

### Test Configuration (application-test.yaml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop  # Clean database for each test run
```

## Next Steps (Phase 1)

1. Generate `data-model.md` with entity definitions
2. Generate OpenAPI contract in `contracts/openapi.yaml`
3. Generate `quickstart.md` with setup instructions
4. Update `CLAUDE.md` with project-specific guidance