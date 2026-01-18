# Implementation Plan: Expense Tracking API

**Branch**: `001-expense-tracking` | **Date**: 2026-01-16 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-expense-tracking/spec.md`

## Summary

Build a RESTful API for expense tracking with full CRUD operations (create, list, retrieve, update, delete). Users can record expenses with description, amount, currency, and date. The API enforces comprehensive validation rules, uses optimistic locking for concurrent updates, and provides standardized error responses. Implementation follows Spring Boot patterns with feature-based architecture, DTO separation, and TDD approach.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.1, Spring Web MVC, Jakarta Validation, Spring Boot Configuration Processor
**Storage**: H2 database (file-based for persistence, in-memory for tests)
**Testing**: JUnit 5, Spring Boot Test (@WebMvcTest, @SpringBootTest), Mockito
**Target Platform**: JVM-based server (Linux/macOS/Windows)
**Project Type**: Single REST API (backend only)
**Performance Goals**: <200ms p95 for create/update/delete, <500ms p95 for list (up to 10k expenses)
**Constraints**: Expense descriptions max 500 chars, amounts max 999,999,999.99 with 2 decimal precision
**Scale/Scope**: Single feature (expenses), 5 REST endpoints, CRUD + optimistic locking

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Gate 1: Feature-Based Architecture ✅ PASS
- **Requirement**: Code organized by business capabilities (features), not technical layers
- **Plan**: Expense tracking will be implemented under `dev.juanvaldivia.moneytrak.expenses` package
- **Structure**: Package will contain controller, service, domain models, DTOs, and mappers
- **Status**: Compliant - follows feature-based organization principle

### Gate 2: DTO Pattern & Clean Contracts ✅ PASS
- **Requirement**: API contracts separated from domain models via explicit DTOs
- **Plan**:
  - `ExpenseCreationDto` for POST requests
  - `ExpenseUpdateDto` for PUT requests
  - `ExpenseDto` for responses
  - `Expense` record for internal domain model
  - `ExpenseMapper` for DTO ↔ Domain conversion
- **Status**: Compliant - full DTO separation planned

### Gate 3: Test-Driven Development (NON-NEGOTIABLE) ✅ PASS
- **Requirement**: Tests written before implementation with Red-Green-Refactor cycle
- **Plan**:
  - Controller tests with `@WebMvcTest` for all 5 endpoints
  - Service unit tests with mocked repositories
  - Integration tests for persistence and validation
  - Validation rule tests for all FR requirements (FR-003 through FR-031)
- **Status**: Compliant - TDD approach committed

### Gate 4: API Versioning & Compatibility ✅ PASS
- **Requirement**: Explicit versioning with `/v1/` path prefix
- **Plan**: All endpoints use `/v1/expenses` base path
- **Status**: Compliant - per spec API Endpoints section

### Gate 5: Type Safety & Validation ✅ PASS
- **Requirement**: Java type system + declarative validation
- **Plan**:
  - Records for DTOs and domain model
  - `BigDecimal` for amounts
  - `Currency` for currency codes (ISO 4217)
  - `ZonedDateTime` for temporal data (stored as UTC)
  - `UUID` for expense identifiers
  - Jakarta Validation annotations on DTOs (`@NotNull`, `@Size`, `@DecimalMin`, `@Digits`)
  - `@Valid` on controller methods
- **Status**: Compliant - full type safety per constitution

### Gate 6: Technology Standards ✅ PASS
- **Requirement**: Java 25, Spring Boot 4.0.1+, Maven wrapper
- **Plan**: Using exact specified versions
- **Build**: `./mvnw clean package` and `./mvnw test`
- **Config**: `application.yaml` for Spring configuration
- **Status**: Compliant

**Overall Gate Status**: ✅ ALL GATES PASS - Proceeding to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/
│   │   └── dev/juanvaldivia/moneytrak/
│   │       ├── MoneyTrakApplication.java          # Spring Boot main class
│   │       └── expenses/                          # Feature package (Constitution: feature-based architecture)
│   │           ├── Expense.java                   # JPA entity (domain model)
│   │           ├── ExpenseController.java         # REST controller (@RestController)
│   │           ├── ExpenseService.java            # Service interface
│   │           ├── LocalExpenseService.java       # Service implementation
│   │           ├── ExpenseRepository.java         # Spring Data JPA repository
│   │           ├── dto/                           # API contracts (Constitution: DTO pattern)
│   │           │   ├── ExpenseCreationDto.java    # POST request
│   │           │   ├── ExpenseUpdateDto.java      # PUT request
│   │           │   ├── ExpenseDto.java            # Response format
│   │           │   └── ErrorResponseDto.java      # Error responses
│   │           ├── mapper/
│   │           │   └── ExpenseMapper.java         # DTO ↔ Domain conversion
│   │           ├── validation/
│   │           │   ├── ValidCurrency.java         # Custom annotation
│   │           │   └── CurrencyValidator.java     # ISO 4217 validator
│   │           └── exception/
│   │               ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   │               ├── NotFoundException.java
│   │               └── ConflictException.java
│   └── resources/
│       ├── application.yaml                       # Main config (H2, JPA)
│       └── application-test.yaml                  # Test config (in-memory H2)
│
└── test/
    └── java/
        └── dev/juanvaldivia/moneytrak/
            └── expenses/
                ├── ExpenseControllerTest.java              # @WebMvcTest (contract)
                ├── ExpenseControllerIntegrationTest.java   # @SpringBootTest (E2E)
                ├── ExpenseServiceTest.java                 # Unit (mocked repo)
                ├── ExpenseRepositoryTest.java              # @DataJpaTest
                ├── ExpenseMapperTest.java                  # Unit
                └── validation/
                    └── CurrencyValidatorTest.java          # Unit

data/                                              # H2 file database (gitignored)
└── moneytrak.mv.db

specs/
└── 001-expense-tracking/
    ├── spec.md                                    # Requirements
    ├── plan.md                                    # This file
    ├── research.md                                # Technology decisions
    ├── data-model.md                              # Entity definitions
    ├── quickstart.md                              # Setup guide
    └── contracts/
        └── openapi.yaml                           # OpenAPI specification
```

**Structure Decision**: Standard Spring Boot Maven project with feature-based package organization per Constitution Gate 1. The `expenses` package is self-contained with controller, service, repository, DTOs, mappers, and validators. This aligns with existing project structure (see `src/main/java/dev/juanvaldivia/moneytrak/expenses/` currently containing `Expense.java`, `ExpenseController.java`, etc.).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No complexity violations. All constitution gates passed without requiring exceptions or justifications.
