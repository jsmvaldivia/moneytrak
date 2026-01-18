# MoneyTrak Constitution

<!--
Sync Impact Report:
Version change: Initial → 1.0.0
Rationale: Initial constitution creation for MoneyTrak project
Modified principles: N/A (new creation)
Added sections: All sections (initial creation)
Removed sections: None
Templates requiring updates:
  ✅ .specify/templates/plan-template.md (verified alignment)
  ✅ .specify/templates/spec-template.md (verified alignment)
  ✅ .specify/templates/tasks-template.md (verified alignment)
Follow-up TODOs: None
-->

## Core Principles

### I. Feature-Based Architecture

Code MUST be organized by business capabilities (features), not technical layers.
Each feature is a self-contained package under `dev.juanvaldivia.moneytrak.<feature>`.

**Rules:**
- Feature packages contain controller, service, domain models, DTOs, and mappers
- Cross-feature dependencies MUST go through public service interfaces only
- No shared "utils" or "common" packages for business logic
- Infrastructure concerns (security, config) remain in dedicated packages

**Rationale:** Feature-based organization reduces coupling, improves discoverability,
and aligns code structure with business domain. Changes to a feature remain localized.

### II. DTO Pattern & Clean Contracts

API contracts MUST be separated from internal domain models via explicit DTOs.

**Rules:**
- Every REST endpoint uses dedicated DTOs (e.g., `ExpenseCreationDto`, `ExpenseDto`)
- Domain models (records like `Expense`) remain internal to the feature
- Mapper classes handle DTO ↔ Domain conversion (e.g., `ExpenseMapper`)
- DTOs enforce validation rules via Jakarta Bean Validation annotations
- Domain models enforce business invariants via constructors/factory methods

**Rationale:** Separation prevents API contracts from leaking into business logic,
enables independent evolution of API and domain, and provides clear validation boundaries.

### III. Test-Driven Development (NON-NEGOTIABLE)

Tests MUST be written before implementation code, with explicit approval gates.

**Rules:**
- Red-Green-Refactor cycle: Write test → Verify it fails → Implement → Verify it passes
- New endpoints require controller tests with `@WebMvcTest`
- Service logic requires unit tests with mocked dependencies
- Integration tests required for: new feature contracts, database interactions, external APIs
- Validation rules MUST have corresponding test cases for valid/invalid inputs
- In the unit tests, the unit of test should cover the business flow and not every class individually.
- Unit tests should avoid IO

**Rationale:** TDD ensures testability by design, provides living documentation,
and catches regressions early. Non-negotiable status prevents technical debt accumulation.

### IV. API Versioning & Compatibility

All REST endpoints MUST use explicit versioning with backward compatibility guarantees.

**Rules:**
- Path-based versioning: `/v1/`, `/v2/`, etc.
- Breaking changes (field removal, type change, semantics change) require new version
- Non-breaking changes (new optional fields, new endpoints) stay in current version
- Deprecation warnings MUST be documented for minimum 2 versions before removal
- Version documented in OpenAPI/Swagger spec

**Rationale:** Explicit versioning protects API consumers from unexpected breakage,
enables parallel version support during migrations, and communicates change impact clearly.

### V. Type Safety & Validation

Leverage Java's type system and declarative validation for correctness.

**Rules:**
- Records for immutable value objects and DTOs
- `BigDecimal` for monetary amounts (never `double`/`float`)
- `Currency` for currency codes (ISO 4217)
- `ZonedDateTime` for temporal data requiring timezone
- `UUID` for entity identifiers
- Jakarta Validation annotations (`@NotNull`, `@PastOrPresent`, `@DecimalMin`, etc.)
  with custom error messages on DTOs
- Controller methods use `@Valid` for automatic validation

**Rationale:** Strong typing prevents entire categories of bugs at compile time.
Declarative validation centralizes rules and provides consistent error responses.

## Technology Standards

### Technology Stack

**Mandatory Technologies:**
- Java 25
- Spring Boot 4.0.1+
- Spring Web MVC for REST APIs
- Jakarta Validation for input validation
- Maven (wrapper required: `mvnw`)

**Build Requirements:**
- All builds MUST succeed via `./mvnw clean package`
- Tests MUST pass via `./mvnw test`
- No warnings allowed in production builds

### Configuration Management

**Rules:**
- `application.yaml` for all Spring configuration (no properties files)
- Spring Boot Configuration Processor enabled for type-safe `@ConfigurationProperties`
- Environment-specific configs via Spring profiles (dev, test, prod)
- Secrets MUST use externalized configuration (environment variables, secret managers)
- No hardcoded credentials, API keys, or sensitive values in source

## Development Workflow

### Code Quality Gates

**Pre-Commit Requirements:**
- All tests pass locally
- No compiler warnings
- Code formatted per project standards

**Pull Request Requirements:**
- Feature branch from `main`
- PR description includes: user story reference, implementation approach, test coverage summary
- All CI checks pass (build, tests, static analysis)
- At least one approval from code owner
- Constitution compliance verified (feature organization, DTO pattern, versioning, validation)

### Testing Strategy

**Test Pyramid:**
- Unit tests (70%): Service logic, mappers, utilities
- Integration tests (20%): Controller + service with mocked externals, database interactions
- End-to-end tests (10%): Full request/response cycles for critical paths

**Coverage Targets:**
- Minimum 80% line coverage for service layer
- 100% coverage for validation rules
- All error handling paths tested

## Governance

### Amendment Process

Constitution changes require:
1. Proposed amendment documented with rationale and impact analysis
2. Review by project maintainers
3. Migration plan for existing code if applicable
4. Approval by majority of core contributors
5. Version bump following semantic versioning

### Versioning Policy

- **MAJOR**: Backward-incompatible principle removal or redefinition requiring code changes
- **MINOR**: New principle/section added or materially expanded guidance
- **PATCH**: Clarifications, wording refinements, typo fixes

### Compliance

- All PRs/reviews MUST verify compliance with constitution principles
- Deviations require explicit justification and approval
- Complexity MUST be justified; simplicity is preferred (YAGNI)
- Use `CLAUDE.md` for agent-specific runtime development guidance

**Version**: 1.0.0 | **Ratified**: 2026-01-16 | **Last Amended**: 2026-01-16