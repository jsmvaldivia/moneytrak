# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION]  
**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]  
**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files or N/A]  
**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIFICATION]  
**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEEDS CLARIFICATION]
**Project Type**: [single/web/mobile - determines source structure]  
**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k lines/sec, 60 fps or NEEDS CLARIFICATION]  
**Constraints**: [domain-specific, e.g., <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]  
**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50 screens or NEEDS CLARIFICATION]

## Database Schema Changes

**Migration Required**: [Yes/No]

**New Tables** (if applicable):
- `table_name`: [Purpose, key columns, relationships]
  - Example: `transactions` - Stores financial transactions with amount, date, category_id FK

**Modified Tables** (if applicable):
- `table_name`: [Columns added/modified/removed, rationale]
  - Example: Add `transaction_type` ENUM column to distinguish INCOME vs EXPENSE

**Data Migration** (if applicable):
- [Describe any data transformation needed]
- Example: Migrate existing `expenses` table data to `transactions` with default type=EXPENSE

**Flyway Migration**:
- Version: `V[next_number]__[snake_case_description].sql`
- Example: `V2__add_transaction_categories.sql`
- Location: `src/main/resources/db/migration/`

**Schema Validation**:
- [ ] Migration tested with existing data (no data loss)
- [ ] Rollback strategy documented (if applicable)
- [ ] Indexes added for foreign keys and frequently queried columns

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

[Gates determined based on constitution file]

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

### Source Code Structure

**MoneyTrak follows a feature-based architecture** where each business capability is a self-contained package.

```text
src/main/java/dev/juanvaldivia/moneytrak/
├── <feature>/                          # Feature package (e.g., transactions, categories)
│   ├── dto/                            # DTOs for this feature
│   │   ├── <Feature>CreationDto.java  # Creation DTO with validation (record)
│   │   ├── <Feature>UpdateDto.java    # Update DTO with version field (record)
│   │   └── <Feature>Dto.java          # Response DTO (record)
│   ├── mapper/                         # Mappers for this feature
│   │   └── <Feature>Mapper.java       # DTO ↔ Entity mapper (static methods)
│   ├── exception/                      # Feature-specific exceptions (optional)
│   │   └── <Feature>Exception.java    # e.g., CategoryInUseException
│   ├── <Feature>.java                  # JPA entity with @Entity, @Version
│   ├── <Feature>Controller.java        # REST controller with @RestController
│   ├── <Feature>Service.java           # Service interface
│   ├── Local<Feature>Service.java      # Service implementation with @Service
│   ├── <Feature>Repository.java        # JPA repository (if custom queries needed)
│   ├── <Feature>Type.java              # Enum if needed (@Enumerated(STRING))
│   └── <Feature>Seeder.java            # Data seeder (if predefined data needed)
├── security/                            # Authentication & authorization (shared)
│   ├── SecurityConfig.java             # SecurityFilterChain bean
│   ├── SecurityProperties.java         # @ConfigurationProperties
│   ├── SecurityUserDetailsService.java # UserDetailsService implementation
│   ├── CustomAuthEntryPoint.java       # 401 error handler
│   └── CustomAccessDeniedHandler.java  # 403 error handler
├── exception/                           # Global error handling (shared)
│   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   ├── NotFoundException.java          # Generic 404 exception
│   ├── ConflictException.java          # Generic 409 exception
│   ├── ErrorResponseDto.java           # Standard error format (record)
│   └── FieldErrorDto.java              # Validation error detail (record)
├── validation/                          # Custom validators (shared)
│   ├── Currency.java                   # Custom validation annotation
│   └── CurrencyValidator.java          # Validator implementation
└── MoneytrakApplication.java           # Spring Boot main class

src/main/resources/
├── application.yaml                     # Spring configuration
├── application-test.yaml                # Test profile configuration
└── db/migration/                        # Flyway migrations
    ├── V1__initial_schema.sql          # Example: Initial schema
    └── V2__add_categories.sql          # Example: Add categories feature

src/test/java/dev/juanvaldivia/moneytrak/
└── <feature>/
    └── <Feature>ControllerIntegrationTest.java  # @SpringBootTest + @AutoConfigureMockMvc
```

**Structure Principles**:
- Each feature is a complete vertical slice with DTOs and mappers in subdirectories
- Cross-feature dependencies go through public service interfaces only
- Shared infrastructure (security, exception, validation) in dedicated packages
- Feature-specific exceptions can live in `<feature>/exception/` subdirectory
- Tests mirror source structure for easy navigation

**For This Feature**:
- Primary package: `dev.juanvaldivia.moneytrak.<feature-name>/`
- Related entities: [List any entities this feature will create]
- Shared infrastructure: [Note if using security/, exception/, validation/ packages]
- Data seeding: [Note if this feature needs predefined data via Seeder class]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
