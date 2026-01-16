# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MoneyTrak is a Spring Boot-based money tracking application built with Java 25. It provides RESTful APIs for expense management.

## Build & Development Commands

### Maven Wrapper
This project uses Maven wrapper (`mvnw`). All commands use `./mvnw` on macOS/Linux or `mvnw.cmd` on Windows.

### Common Commands
- **Build the project**: `./mvnw clean package`
- **Run the application**: `./mvnw spring-boot:run`
- **Run tests**: `./mvnw test`
- **Run a single test**: `./mvnw test -Dtest=ClassName#methodName`
- **Compile only**: `./mvnw compile`
- **Clean build directory**: `./mvnw clean`

## Architecture

### Technology Stack
- Spring Boot 4.0.1
- Java 25
- Spring Web MVC for REST APIs
- Spring REST Client for HTTP communication
- Jakarta Validation for request validation
- Configuration processor enabled for type-safe configuration properties

### Package Structure
The codebase follows a feature-based package structure under `dev.juanvaldivia.moneytrak`:
- `expenses/` - Expense management feature with controller, service, domain models, and DTOs

### Core Patterns

**Service Layer Pattern**: Interface-based services (e.g., `ExpenseService` interface with `LocalExpenseService` implementation) enable multiple implementations and easier testing.

**DTO Pattern**: Separate DTOs for API contracts:
- `ExpenseCreationDto` - Input validation with Jakarta Bean Validation annotations
- `ExpenseDto` - API response format
- `Expense` - Internal domain model (record)
- `ExpenseMapper` - Maps between DTOs and domain models

**Controller Design**: REST controllers use:
- `@Valid` for automatic validation of request bodies
- `ResponseEntity` for HTTP response control
- `ServletUriComponentsBuilder` for generating resource URIs in `Location` headers

### Domain Model Conventions
- Records are used for immutable data structures (e.g., `Expense`, `ExpenseDto`)
- `BigDecimal` for monetary amounts with precision constraints
- `Currency` type for currency codes
- `ZonedDateTime` for temporal data with timezone support
- UUID for entity identifiers

### Validation Rules
Validation is declarative using Jakarta Validation annotations on DTOs:
- `@NotNull`, `@PastOrPresent`, `@Size`, `@DecimalMin`, `@Digits` control input constraints
- Custom error messages provided for each validation rule

### API Versioning
REST endpoints are versioned using path prefix `/v1/` (e.g., `/v1/expenses`).

### Configuration
- `application.yaml` in `src/main/resources/` for Spring configuration
- Spring Boot Configuration Processor generates metadata for IDE autocomplete