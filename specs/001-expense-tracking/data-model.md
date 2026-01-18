# Data Model: Expense Tracking API

**Feature**: 001-expense-tracking
**Date**: 2026-01-16
**Phase**: 1 (Design & Contracts)

## Entity: Expense

### Purpose
Represents a single spending transaction with complete audit trail and concurrency control.

### JPA Entity Definition

```java
package dev.juanvaldivia.moneytrak.expenses;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public record Expense(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id,

    @Column(nullable = false, length = 500)
    String description,

    @Column(nullable = false, precision = 11, scale = 2)
    BigDecimal amount,

    @Column(nullable = false, length = 3)
    String currency,  // ISO 4217 code (e.g., "USD", "EUR")

    @Column(nullable = false)
    ZonedDateTime date,  // Transaction date (stored in UTC)

    @Version
    @Column(nullable = false)
    Integer version,  // Optimistic locking version (auto-managed by JPA)

    @Column(nullable = false, updatable = false)
    ZonedDateTime createdAt,

    @Column(nullable = false)
    ZonedDateTime updatedAt
) {
    // JPA requires no-arg constructor for records
    public Expense {
        // JPA will call this constructor
    }

    // Factory method for creation (sets timestamps)
    public static Expense create(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date
    ) {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        return new Expense(
            null,  // JPA will generate ID
            description,
            amount,
            currency,
            date.withZoneSameInstant(java.time.ZoneOffset.UTC),  // Normalize to UTC
            null,  // JPA will initialize version to 0
            now,   // createdAt
            now    // updatedAt
        );
    }

    // Factory method for updates (updates timestamp)
    public Expense update(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date
    ) {
        return new Expense(
            this.id,
            description,
            amount,
            currency,
            date.withZoneSameInstant(java.time.ZoneOffset.UTC),
            this.version,  // JPA will increment automatically
            this.createdAt,  // Preserve original
            ZonedDateTime.now(java.time.ZoneOffset.UTC)  // Update timestamp
        );
    }
}
```

### Field Specifications

| Field | Type | Nullable | Constraints | Purpose |
|-------|------|----------|-------------|---------|
| `id` | UUID | No | Primary key, auto-generated | Unique expense identifier (FR-002) |
| `description` | String | No | Max 500 chars | What was purchased (FR-001, FR-006, FR-018, FR-027) |
| `amount` | BigDecimal | No | Precision 11, Scale 2, >0, ≤999,999,999.99 | Cost of expense (FR-001, FR-003, FR-028) |
| `currency` | String | No | Exactly 3 chars, ISO 4217 code | Monetary unit (FR-001, FR-004) |
| `date` | ZonedDateTime | No | Not future, stored in UTC | When transaction occurred (FR-001, FR-005, FR-020, FR-021) |
| `version` | Integer | No | Auto-managed by JPA | Optimistic locking version (FR-016, FR-022, FR-023) |
| `createdAt` | ZonedDateTime | No | Auto-set on create, immutable | Record creation timestamp (FR-029) |
| `updatedAt` | ZonedDateTime | No | Auto-updated on modify | Last modification timestamp (FR-030) |

### Validation Rules

Enforced at DTO level (not entity level) per constitution's separation principle:

1. **Description** (FR-006, FR-018, FR-027):
   - Not null or empty
   - Maximum 500 characters
   - Reject if exceeded

2. **Amount** (FR-003, FR-019, FR-026, FR-028):
   - Not null
   - Positive (> 0)
   - Maximum 2 decimal places
   - Maximum value 999,999,999.99
   - Reject if zero, negative, or >2 decimals

3. **Currency** (FR-004):
   - Not null
   - Valid ISO 4217 3-letter code
   - Validated via `Currency.getInstance(code)`

4. **Date** (FR-005, FR-020, FR-021, FR-025):
   - Not null
   - ISO 8601 format with timezone
   - Not in the future
   - Stored as UTC internally

5. **Version** (FR-017, FR-024):
   - Must match current version on update
   - 409 Conflict if mismatch

### State Transitions

```
[New] --create()--> [Persisted v1]
                        |
                        +--update()--> [Persisted v2]
                        |
                        +--update()--> [Persisted v3]
                        |
                        +--delete()--> [Deleted]
```

- **Create**: Sets `id`, `version=0` (JPA increments to 1), `createdAt`, `updatedAt`
- **Update**: Increments `version`, updates `updatedAt`, preserves `createdAt`
- **Delete**: Removes from database (hard delete, no soft delete requirement)

### Indexes

```sql
-- Primary key index (automatic)
CREATE UNIQUE INDEX pk_expenses ON expenses(id);

-- Query optimization for list endpoint (newest first)
CREATE INDEX idx_expenses_date_desc ON expenses(date DESC);
```

## DTOs (Data Transfer Objects)

### ExpenseCreationDto (POST /v1/expenses)

```java
package dev.juanvaldivia.moneytrak.expenses.dto;

import dev.juanvaldivia.moneytrak.expenses.validation.ValidCurrency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseCreationDto(
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places and not exceed 999,999,999.99")
    BigDecimal amount,

    @NotNull(message = "Currency is required")
    @ValidCurrency  // Custom validator
    String currency,

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date
) {}
```

### ExpenseUpdateDto (PUT /v1/expenses/{id})

```java
package dev.juanvaldivia.moneytrak.expenses.dto;

import dev.juanvaldivia.moneytrak.expenses.validation.ValidCurrency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseUpdateDto(
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,  // Optional for partial updates

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places")
    BigDecimal amount,  // Optional for partial updates

    @ValidCurrency
    String currency,  // Optional for partial updates

    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date,  // Optional for partial updates

    @NotNull(message = "Version is required for optimistic locking")
    Integer version  // Required for version check
) {}
```

### ExpenseDto (Response format)

```java
package dev.juanvaldivia.moneytrak.expenses.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ExpenseDto(
    UUID id,
    String description,
    BigDecimal amount,
    String currency,
    ZonedDateTime date,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
```

### Error Response DTOs

```java
package dev.juanvaldivia.moneytrak.expenses.dto;

import java.util.List;

public record ErrorResponseDto(
    int status,
    String error,  // "ValidationError", "NotFound", "Conflict", "InternalError"
    String message,
    List<FieldErrorDto> details
) {}

public record FieldErrorDto(
    String field,
    String message
) {}
```

## Mapper

```java
package dev.juanvaldivia.moneytrak.expenses.mapper;

import dev.juanvaldivia.moneytrak.expenses.Expense;
import dev.juanvaldivia.moneytrak.expenses.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseCreationDto dto) {
        return Expense.create(
            dto.description(),
            dto.amount(),
            dto.currency(),
            dto.date()
        );
    }

    public Expense toEntity(Expense existing, ExpenseUpdateDto dto) {
        return existing.update(
            dto.description() != null ? dto.description() : existing.description(),
            dto.amount() != null ? dto.amount() : existing.amount(),
            dto.currency() != null ? dto.currency() : existing.currency(),
            dto.date() != null ? dto.date() : existing.date()
        );
    }

    public ExpenseDto toDto(Expense entity) {
        return new ExpenseDto(
            entity.id(),
            entity.description(),
            entity.amount(),
            entity.currency(),
            entity.date(),
            entity.version(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }
}
```

## Custom Validation

### @ValidCurrency Annotation

```java
package dev.juanvaldivia.moneytrak.expenses.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
@Documented
public @interface ValidCurrency {
    String message() default "Invalid ISO 4217 currency code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### CurrencyValidator

```java
package dev.juanvaldivia.moneytrak.expenses.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // @NotNull handles null check
        }

        try {
            Currency.getInstance(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;  // Invalid ISO 4217 code
        }
    }
}
```

## Repository

```java
package dev.juanvaldivia.moneytrak.expenses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // FR-013: List all expenses in reverse chronological order
    @Query("SELECT e FROM Expense e ORDER BY e.date DESC")
    List<Expense> findAllOrderByDateDesc();
}
```

## Database Schema (H2/PostgreSQL compatible)

```sql
CREATE TABLE expenses (
    id              UUID PRIMARY KEY,
    description     VARCHAR(500) NOT NULL,
    amount          DECIMAL(11, 2) NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3) NOT NULL,
    date            TIMESTAMP WITH TIME ZONE NOT NULL,
    version         INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_expenses_date_desc ON expenses(date DESC);
```

## Relationships

This feature has no relationships to other entities (single-entity feature). Future expansion may add:

- **Categories**: One expense → One category (optional)
- **Users**: One expense → One user (when multi-user support added)
- **Attachments**: One expense → Many receipts (when receipt upload added)

These are currently out of scope per spec.md Out of Scope section.