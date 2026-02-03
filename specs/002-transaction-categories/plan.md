# Implementation Plan: Transaction Categories and Types

**Branch**: `002-transaction-categories` | **Date**: 2026-01-19 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-transaction-categories/spec.md`

## Summary

Enhance MoneyTrak to support transaction categories (14 predefined + custom), transaction types (EXPENSE/INCOME), and transaction stability (FIXED/VARIABLE). Migrate the existing Expense domain model to Transaction, rename the database table via Flyway migration, and replace `/v1/expenses` endpoints with `/v1/transactions`. The approach uses Flyway SQL migrations for all DDL and data changes, JPA `@Enumerated(EnumType.STRING)` for enum persistence, `@ManyToOne` for the Transaction-Category relationship, and a `CommandLineRunner` to seed predefined categories.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.1, Spring Web MVC, Spring Data JPA, Jakarta Validation, Hibernate ORM 7.2.0, Flyway
**Storage**: H2 Database (file-based `jdbc:h2:file:./data/moneytrak` for production, in-memory for tests)
**Testing**: Spring Boot Test (`@SpringBootTest`, `@AutoConfigureMockMvc`), Maven Surefire via `./mvnw test`
**Target Platform**: JVM (local development server, localhost:8080)
**Project Type**: Single Spring Boot application (Maven)
**Performance Goals**: Category filtering < 2 seconds response time (SC-003); < 100 transactions/day expected load
**Constraints**: No external services; H2 limitations (no functional indexes for case-insensitive uniqueness — enforced at application level)
**Scale/Scope**: Single-user to small-team personal finance tracker; ~30k transactions/year projected

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0 Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Feature-Based Architecture | PASS | New `categories/` and `transactions/` packages follow feature-based organization. Cross-feature dependency (Transaction → Category) goes through CategoryService/CategoryRepository interfaces. |
| II. DTO Pattern & Clean Contracts | PASS | Separate DTOs for each feature: `CategoryCreationDto`, `CategoryUpdateDto`, `CategoryDto`, `TransactionCreationDto`, `TransactionUpdateDto`, `TransactionDto`. Mapper classes (`CategoryMapper`, `TransactionMapper`) handle conversions. Jakarta Validation annotations on all DTOs. |
| III. Test-Driven Development | PASS | Tasks explicitly require tests written FIRST (Red-Green-Refactor). Integration tests for controllers, unit tests for service logic. TDD is non-negotiable per constitution. |
| IV. API Versioning & Compatibility | **VIOLATION** | FR-031 removes `/v1/expenses` with no deprecation. Constitution requires "minimum 2 versions before removal." See Complexity Tracking for justification. |
| V. Type Safety & Validation | PASS | `BigDecimal` for amounts, `UUID` for IDs, `ZonedDateTime` for dates, `@Enumerated(EnumType.STRING)` for enums, Jakarta Validation on all DTOs with custom messages. |
| Technology Stack | PASS | Java 25, Spring Boot 4.0.1, Maven wrapper, application.yaml configuration. |
| Build Requirements | PASS | `./mvnw clean package` and `./mvnw test` required to pass. No warnings allowed. |

### Post-Phase 1 Re-Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Feature-Based Architecture | PASS | `categories/` package contains: entity, repository, service interface, service impl, controller, DTOs, mapper, exception, seeder. `transactions/` package contains: entity, repository, service interface, service impl, controller, DTOs, mapper, enums. Shared exception handling in `expenses/exception/` (legacy location, acceptable for infrastructure). |
| II. DTO Pattern & Clean Contracts | PASS | OpenAPI spec (contracts/openapi.yaml) documents all DTOs. Response DTOs use records. Category entity is mutable JPA class with `@Version` for optimistic locking. |
| III. Test-Driven Development | PASS | Tasks organized as: write tests → verify failure → implement → verify pass. Integration tests per user story + final cross-cutting validation. |
| IV. API Versioning & Compatibility | **JUSTIFIED VIOLATION** | See Complexity Tracking below. Pre-1.0 API (version 0.0.1-SNAPSHOT), no external consumers, user explicitly requested clean break. |
| V. Type Safety & Validation | PASS | Data model uses correct types. Custom `@ValidCurrency` annotation. Enums stored as STRING with VARCHAR(32). |

## Project Structure

### Documentation (this feature)

```text
specs/002-transaction-categories/
├── plan.md              # This file
├── research.md          # Phase 0 output (complete)
├── data-model.md        # Phase 1 output (complete)
├── quickstart.md        # Phase 1 output (complete)
├── contracts/
│   └── openapi.yaml     # Phase 1 output (complete)
├── checklists/          # Custom checklists
└── tasks.md             # Phase 2 output (complete)
```

### Source Code (repository root)

```text
src/main/java/dev/juanvaldivia/moneytrak/
├── MoneytrakApplication.java
├── categories/                          # US1: Category management feature
│   ├── Category.java                    # JPA entity
│   ├── CategoryRepository.java          # Spring Data JPA repository
│   ├── CategoryService.java             # Service interface
│   ├── LocalCategoryService.java        # Service implementation
│   ├── CategoryController.java          # REST controller (/v1/categories)
│   ├── CategorySeeder.java              # CommandLineRunner for 14 predefined categories
│   ├── dto/
│   │   ├── CategoryCreationDto.java     # POST request DTO
│   │   ├── CategoryUpdateDto.java       # PUT request DTO
│   │   └── CategoryDto.java             # Response DTO
│   ├── mapper/
│   │   └── CategoryMapper.java          # DTO ↔ Entity conversion
│   └── exception/
│       └── CategoryInUseException.java  # 409 when deleting category with transactions
├── transactions/                        # US2-US4: Transaction management feature
│   ├── Transaction.java                 # JPA entity (renamed from Expense)
│   ├── TransactionRepository.java       # Spring Data JPA repository
│   ├── TransactionService.java          # Service interface
│   ├── LocalTransactionService.java     # Service implementation
│   ├── TransactionController.java       # REST controller (/v1/transactions)
│   ├── TransactionType.java             # Enum: EXPENSE, INCOME
│   ├── TransactionStability.java        # Enum: FIXED, VARIABLE
│   ├── dto/
│   │   ├── TransactionCreationDto.java  # POST request DTO
│   │   ├── TransactionUpdateDto.java    # PUT request DTO
│   │   └── TransactionDto.java          # Response DTO
│   └── mapper/
│       └── TransactionMapper.java       # DTO ↔ Entity conversion
├── expenses/                            # Shared infrastructure (legacy)
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── validation/
│   │   └── ValidCurrency.java           # Custom validation annotation
│   └── dto/
│       └── ErrorResponse DTOs           # Shared error response format
├── migration/                           # US5: Migration support
│   └── (migration service/runner if needed)
└── config/                              # Infrastructure

src/main/resources/
├── application.yaml                     # Spring Boot configuration
└── db/migration/
    ├── V1__create_expenses_table.sql    # Initial schema (existing)
    └── V2__add_categories_and_types.sql # Migration: categories table, rename expenses→transactions, add columns

src/test/java/dev/juanvaldivia/moneytrak/
├── categories/
│   ├── CategoryControllerTest.java      # US1 controller integration tests
│   └── CategoryIntegrationTest.java     # US1 seeding verification
├── transactions/
│   ├── TransactionControllerTest.java   # US2-US4 controller integration tests
│   └── TransactionServiceTest.java      # US3 summary calculation tests
├── migration/
│   └── DataMigrationTest.java           # US5 migration verification tests
└── FinalIntegrationTest.java            # Cross-cutting end-to-end tests
```

**Structure Decision**: Single Spring Boot project with feature-based package organization. Categories and transactions are separate feature packages following Constitution Principle I. Migration support is a separate package for isolation.

## Complexity Tracking

> Constitution Check Principle IV violation justification

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Principle IV: Breaking API change without deprecation period (FR-031 removes `/v1/expenses` immediately) | Pre-release API (version 0.0.1-SNAPSHOT) with no external consumers. User explicitly requested clean break: "Replace immediately with `/v1/transactions`, no backward compatibility" (spec clarification 2026-01-19). Maintaining deprecated endpoints would add unused code. | Adding `/v2/transactions` while keeping `/v1/expenses`: rejected because it creates dead code with no consumers, contradicts user's explicit preference, and violates YAGNI (Constitution Governance: "Complexity MUST be justified; simplicity is preferred"). |

## Design Decisions

### 1. Database Migration: Flyway SQL

**Decision**: Use Flyway migrations for all schema changes (DDL + DML).

**Rationale**: The project already has Flyway as a dependency (pom.xml). Flyway provides repeatable, version-controlled migrations that work consistently across environments. The V2 migration handles: creating the categories table, seeding 14 predefined categories, renaming `expenses` → `transactions`, adding `transaction_type`, `transaction_stability`, and `category_id` columns, and setting defaults for existing data.

**Note**: `spring.jpa.hibernate.ddl-auto: update` remains for development convenience but Flyway handles the actual schema evolution. For production, ddl-auto should be set to `validate`.

### 2. Category Seeding: Dual Strategy

**Decision**: Seed predefined categories via both Flyway SQL (V2 migration) and `CommandLineRunner` (CategorySeeder).

**Rationale**: Flyway handles seeding for the migration path (existing databases). CategorySeeder handles the fresh-database path and serves as a safety net. The seeder checks `categoryRepository.count() == 0` to avoid duplicating Flyway-seeded data.

### 3. Transaction-Category Relationship: Unidirectional @ManyToOne

**Decision**: Transaction has `@ManyToOne` to Category. Category does not have `@OneToMany` to Transaction.

**Rationale**: Avoids N+1 query issues on the Category side. Transaction count for deletion validation uses `transactionRepository.countByCategoryId()` instead of `category.getTransactions().size()`. Simpler entity design per YAGNI.

### 4. Enum Storage: @Enumerated(EnumType.STRING)

**Decision**: Both `TransactionType` and `TransactionStability` stored as VARCHAR(32) strings.

**Rationale**: Extensible (can add values without data migration), readable in database, no ordinal corruption risk. Per research.md findings.

### 5. Default Values

| Field | Default | When Applied |
|-------|---------|-------------|
| `transactionType` | EXPENSE | Not specified on creation (per CLAUDE.md); required in OpenAPI spec |
| `transactionStability` | VARIABLE | Not specified on creation (FR-025) |
| `categoryId` | "Others" category UUID | Not specified on creation (FR-014) |

### 6. Error Code Alignment

| Scenario | HTTP Status | Rationale |
|----------|-------------|-----------|
| Invalid category ID on transaction create/update | 404 Not Found | Category resource doesn't exist (FR-015, spec clarification) |
| Duplicate category name | 409 Conflict | Resource conflict (FR-008a) |
| Delete category with transactions | 409 Conflict | Constraint violation (FR-006) |
| Optimistic lock version mismatch | 409 Conflict | Concurrent modification conflict |
| Validation errors | 400 Bad Request | Invalid input data |

## Phases Summary

### Phase 0: Research (Complete)

All technical decisions documented in [research.md](./research.md):
- Database schema evolution strategy (Flyway + Hibernate)
- Enum handling in JPA (`@Enumerated(EnumType.STRING)`)
- Category management design (seeding, uniqueness, deletion rules)
- Transaction-category linking (default behavior, filtering)
- Amount validation (positive only, `BigDecimal`)
- API migration strategy (clean break, no backward compat)
- JPA entity design (mutable classes, `@Version`, factory methods)
- Performance considerations (indexing, `@EntityGraph`)

### Phase 1: Design & Contracts (Complete)

Artifacts produced:
- [data-model.md](./data-model.md) — Entity definitions, relationships, migration strategy, query patterns
- [contracts/openapi.yaml](./contracts/openapi.yaml) — OpenAPI 3.0.3 spec for all endpoints
- [quickstart.md](./quickstart.md) — curl examples for manual testing

### Phase 2: Tasks (Complete)

96 tasks organized by user story in [tasks.md](./tasks.md). See tasks.md for full execution plan with dependencies, parallelization markers, and TDD gates.

## Known Issues from Analysis

The following issues were identified during `/speckit.analyze` and should be addressed:

1. **FR-019 coverage gap**: No task for transaction type filtering (test + implementation). Add to Phase 5.
2. **Unresolved edge cases**: spec.md L126 (NULL currency on migration) and L128 (concurrent category name updates) need resolution.
3. **data-model.md terminology drift**: Uses "expense types" in places; should be "transaction stability" consistently.
4. **OpenAPI/spec alignment**: `transactionStability` listed as required in OpenAPI but defaults to VARIABLE per spec FR-025. OpenAPI should mark it optional.
5. **Flyway migration sequencing**: T083 modifies V2 migration after T007 created it. Must be done before V2 is applied to any database, or use a separate V3 migration.