# Implementation Plan: Simple Role-Based Authentication

**Branch**: `003-simple-auth` | **Date**: 2026-02-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-simple-auth/spec.md`

## Summary

Add HTTP Basic Authentication with three roles (APP, BACKOFFICE, ADMIN) to secure all existing MoneyTrak API endpoints. Users are defined in application configuration (not database). APP gets read-only access, BACKOFFICE gets full CRUD, ADMIN gets CRUD + actuator endpoints. Health check remains public. Error responses match existing `ErrorResponseDto` format.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.1, Spring Security 7.0.2 (via `spring-boot-starter-security`), Spring Web MVC, Spring Data JPA, Jakarta Validation
**Storage**: H2 (file-based production, in-memory tests) — no schema changes for this feature
**Testing**: `./mvnw test` — JUnit 5 + MockMvc + `spring-security-test` (`@WithMockUser`, `httpBasic()`)
**Target Platform**: JVM (server-side REST API)
**Project Type**: Single Spring Boot application
**Performance Goals**: N/A — security layer adds negligible overhead for in-memory user lookup
**Constraints**: No database migration required. No new API endpoints. Configuration-only users (< 10).
**Scale/Scope**: 3 users, 3 roles, 5 security classes, ~6 existing test files to update, 1 new test file

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Feature-Based Architecture | PASS | Security classes go in `dev.juanvaldivia.moneytrak.security` package (infrastructure concern, permitted by constitution) |
| II. DTO Pattern & Clean Contracts | PASS | Error responses reuse existing `ErrorResponseDto` record. No new DTOs needed. |
| III. Test-Driven Development | PASS | New `SecurityIntegrationTest` covers all role/endpoint combinations. Existing tests updated with `@WithMockUser`. TDD cycle: tests written first for security scenarios. |
| IV. API Versioning & Compatibility | PASS | No new endpoints introduced. No breaking changes to existing endpoints. Security is an additive layer. |
| V. Type Safety & Validation | PASS | `SecurityProperties` is a type-safe `@ConfigurationProperties` record. Role names validated via configuration binding. |
| Configuration Management | PASS | Users defined in `application.yaml` with BCrypt-hashed passwords. Test profile uses `{noop}` for readability. No hardcoded credentials in source. |

**Pre-Phase 0 Gate**: PASS
**Post-Phase 1 Gate**: PASS — Design aligns with all constitution principles.

## Project Structure

### Documentation (this feature)

```text
specs/003-simple-auth/
├── plan.md              # This file
├── research.md          # Phase 0 output (complete)
├── data-model.md        # Phase 1 output (complete)
├── quickstart.md        # Phase 1 output (complete)
├── contracts/
│   └── security-rules.md  # Phase 1 output (complete)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
src/main/java/dev/juanvaldivia/moneytrak/
├── security/
│   ├── SecurityConfig.java              # SecurityFilterChain bean with URL-based rules
│   ├── SecurityProperties.java          # @ConfigurationProperties record for user config
│   ├── SecurityUserDetailsService.java  # Maps config users → InMemoryUserDetailsManager
│   ├── CustomAuthEntryPoint.java        # JSON 401 response handler + failed auth logging
│   └── CustomAccessDeniedHandler.java   # JSON 403 response handler
├── exception/
│   └── ErrorResponseDto.java            # Existing — reused by auth handlers
├── transactions/
│   └── TransactionController.java       # Existing — no changes needed
└── categories/
    └── CategoryController.java          # Existing — no changes needed

src/main/resources/
├── application.yaml                     # Updated: moneytrak.security.users config block
└── application-test.yaml                # Updated: test users with {noop} passwords

src/test/java/dev/juanvaldivia/moneytrak/
├── security/
│   └── SecuritySmokeTest.java           # Existing smoke test (2 tests)
│   └── SecurityIntegrationTest.java     # NEW: comprehensive role-based tests
├── transactions/
│   └── TransactionControllerTest.java   # Updated: @WithMockUser(roles = "ADMIN")
├── categories/
│   ├── CategoryControllerTest.java      # Updated: @WithMockUser(roles = "ADMIN")
│   └── CategoryIntegrationTest.java     # Updated: @WithMockUser(roles = "ADMIN")
├── FinalIntegrationTest.java            # Updated: @WithMockUser(roles = "ADMIN")
└── MoneytrakApplicationTests.java       # Updated: @WithMockUser(roles = "ADMIN")
```

**Structure Decision**: Feature-based package `security/` under the main application package, consistent with the existing `transactions/` and `categories/` feature packages. Infrastructure concern as allowed by Constitution Principle I.

## Current Implementation Status

The security infrastructure is **already implemented** on this branch. Five source files exist in the `security/` package, dependencies are added to `pom.xml`, and configuration is in both YAML files. However:

**What's done**:
- `SecurityConfig.java` — SecurityFilterChain with correct authorization rules
- `SecurityProperties.java` — `@ConfigurationProperties` record
- `SecurityUserDetailsService.java` — InMemoryUserDetailsManager wired from config
- `CustomAuthEntryPoint.java` — JSON 401 handler with failed auth logging (FR-014)
- `CustomAccessDeniedHandler.java` — JSON 403 handler
- `pom.xml` — `spring-boot-starter-security` and `spring-security-test` added
- `application.yaml` — 3 users with BCrypt passwords configured
- `application-test.yaml` — 3 test users with `{noop}` passwords
- `SecuritySmokeTest.java` — 2 basic tests (auth 200, no-auth 401)
- Existing test classes have `@WithMockUser(roles = "ADMIN")` at class level

**What's broken** (27 of 31 tests failing with 401):
- The `@WithMockUser(roles = "ADMIN")` annotation is present on all test classes, but tests using `@AutoConfigureMockMvc` are still getting 401 responses. This indicates a Spring Security 7 / Spring Boot 4 compatibility issue where the auto-configured MockMvc may not be picking up the security context from `@WithMockUser`.
- The `SecuritySmokeTest` passes because it manually configures MockMvc with `springSecurity()` in `@BeforeEach`.

**What needs to be done**:
1. Fix the test infrastructure so `@AutoConfigureMockMvc` + `@WithMockUser` works with Spring Security 7
2. Write comprehensive `SecurityIntegrationTest` covering all acceptance scenarios from the spec
3. Verify all 5 user stories' acceptance criteria pass
4. Update CLAUDE.md with the security feature documentation

## Complexity Tracking

No constitution violations to justify — all design decisions align with existing principles.