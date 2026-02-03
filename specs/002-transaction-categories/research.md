# Research: Transaction Categories and Types

**Feature**: 002-transaction-categories
**Date**: 2026-01-19
**Status**: Complete

## Executive Summary

This document consolidates research for implementing transaction categories, transaction/expense types, and migrating from the Expense to Transaction domain model. Key decisions focus on enum handling best practices, JPA entity relationships, and development-friendly database schema evolution using Hibernate DDL auto-generation.

## 1. Database Schema Evolution Strategy

### Decision: Hibernate DDL Auto with Manual Data Migration

**Chosen Approach**: Continue using `spring.jpa.hibernate.ddl-auto: update` with manual data migration script for renaming entities.

**Rationale**:
- **Simplicity**: No migration framework dependency needed for current development phase
- **Development Speed**: Hibernate auto-generates schema changes from JPA entities
- **Sufficient for Current Stage**: H2 database with manageable data volumes
- **Future Path**: Can adopt Flyway later when production deployment requires zero-downtime migrations

### Key Implementation Steps

1. **Create new entities** (`Transaction`, `Category`) alongside existing `Expense` entity
2. **Hibernate auto-creates tables** (`transactions`, `categories`) on application startup
3. **Manual data migration script**: Copy data from `expenses` to `transactions` with defaults
4. **Remove old entity**: Delete `Expense` entity class after successful migration
5. **Hibernate drops table**: `expenses` table automatically dropped on next restart

### Data Migration Script

**One-time SQL script** (run after new entities created):

```sql
-- Copy existing expenses to transactions with default values
INSERT INTO transactions (
    id, description, amount, currency, date,
    transaction_type, transaction_stability, category_id,
    version, created_at, updated_at
)
SELECT
    e.id,
    e.description,
    e.amount,
    e.currency,
    e.date,
    'EXPENSE',  -- Default TransactionType
    'VARIABLE', -- Default TransactionStability
    (SELECT id FROM categories WHERE name = 'Others'),  -- Default category
    e.version,
    e.created_at,
    e.updated_at
FROM expenses e
WHERE NOT EXISTS (
    SELECT 1 FROM transactions t WHERE t.id = e.id
);
```

**Execution**: Run via H2 Console (`http://localhost:8080/h2-console`) or programmatically via `@PostConstruct` bean.

### Rollback Plan

**Development Environment**: Database backed up to `data/` directory, can restore from file system.

**Simple Rollback**:
```bash
# Backup before migration
cp data/moneytrak.mv.db data/moneytrak.mv.db.backup-$(date +%Y%m%d)

# Rollback if needed
cp data/moneytrak.mv.db.backup-20260119 data/moneytrak.mv.db
```

---

## 2. Enum Handling in JPA

### Decision: @Enumerated(EnumType.STRING)

**Chosen Approach**: Use `@Enumerated(EnumType.STRING)` with optimized column sizing for both `TransactionType` and `TransactionStability`.

```java
@Enumerated(EnumType.STRING)
@Column(name = "transaction_type", length = 32, nullable = false)
private TransactionType transactionType;

@Enumerated(EnumType.STRING)
@Column(name = "transaction_stability", length = 32, nullable = true)
private TransactionStability transactionStability;
```

**Rationale**:
- **Extensibility**: Can add/reorder enum values without breaking existing data
- **Readability**: Database shows meaningful names ("EXPENSE", "INCOME") not numbers (0, 1)
- **Safety**: No silent data corruption when enum order changes
- **Maintainability**: Only restriction is renaming values requires data update (acceptable tradeoff)
- **Performance**: Minimal overhead vs ORDINAL; indexing has 100x more impact

**Alternative Considered: @Enumerated(EnumType.ORDINAL)**
- **Rejected because**: Adding/removing/reordering enum values corrupts existing data (brittle)

**Alternative Considered: AttributeConverter with Custom Codes**
- Could use single-character codes (e.g., 'I' for INCOME, 'E' for EXPENSE)
- **Rejected because**: Additional code complexity not justified for our simple enums

### TransactionStability Validation

**Solution**: TransactionStability is required for all transactions (both EXPENSE and INCOME):

```java
public record TransactionCreationDto(
    @NotNull TransactionType transactionType,
    @NotNull TransactionStability transactionStability  // Required for all types
) {}
```

**Usage Examples**:
- EXPENSE + FIXED: Rent, subscriptions, insurance
- EXPENSE + VARIABLE: Groceries, gas, entertainment
- INCOME + FIXED: Salary, pension
- INCOME + VARIABLE: Freelance payments, bonuses

### JSON Serialization & Validation

**REST API Error Handling**:
Jackson throws `HttpMessageNotReadableException` for invalid enum values before validation runs.

**Solution**: Global exception handler for graceful error messages:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEnum(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid enum value: " + ife.getValue())
            );
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request format"));
    }
}
```

### Common Pitfalls Avoided

1. ❌ **Using ORDINAL (JPA default)** → Always explicitly use `EnumType.STRING`
2. ❌ **Not constraining VARCHAR length** → Always specify `@Column(length = 32)`
3. ❌ **Renaming enum values without data update** → Requires SQL script to update existing records
4. ❌ **Not handling JSON deserialization errors** → Add global exception handler
5. ❌ **Removing enum values without checking DB** → Could break existing records

---

## 3. Category Management Design

### Predefined vs Custom Categories

**Decision**: Support both predefined (seeded on initialization) and user-created custom categories.

**Predefined Categories** (14 total, from FR-001):
- Office Renting, Public Transport, Bank, Car Maintenance
- Food & Drinks, Subscriptions, Supermarket, Tolls
- Gas, Sport, Gifts, ATM, Video & Films, Transfers, Others

**Seeding Strategy**:
```java
@Component
public class CategorySeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            List<Category> predefined = List.of(
                Category.createPredefined("Office Renting"),
                Category.createPredefined("Public Transport"),
                Category.createPredefined("Bank"),
                // ... all 14 categories
            );
            categoryRepository.saveAll(predefined);
        }
    }
}
```

**Rationale**:
- Predefined categories provide immediate value without user setup
- `isPredefined` flag distinguishes system vs user-created (useful for UI/reporting)
- Users can still rename predefined categories (clarification answer: Option A)
- Seeded on first startup via `CommandLineRunner`

### Category Uniqueness

**Decision**: Enforce unique category names (case-insensitive) with HTTP 409 Conflict on duplicates.

**Implementation**:
```java
@Table(name = "categories")
public class Category {
    @Column(nullable = false, length = 100)
    private String name;
}

// Uniqueness check in service layer (case-insensitive)
if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
    throw new ConflictException("Category with name '" + dto.name() + "' already exists");
}
```

**Note**: H2 doesn't support functional indexes for case-insensitive uniqueness, so enforced at application level.

**Error Response**: HTTP 409 Conflict (clarification answer: Option A)

### Category Deletion Rules

**Decision**: Prevent deletion of categories with associated transactions (FR-006).

**Implementation**:
```java
public void deleteCategory(UUID id) {
    Category category = findById(id);

    long transactionCount = transactionRepository.countByCategoryId(id);
    if (transactionCount > 0) {
        throw new ConflictException(
            "Cannot delete category with " + transactionCount + " associated transactions"
        );
    }

    categoryRepository.delete(category);
}
```

**Rationale**: Prevents orphaned transactions, maintains data integrity

---

## 4. Transaction-Category Linking

### Default Category Behavior

**Decision**: Assign "Others" category when no category specified (FR-014).

**Implementation**:
```java
public Transaction create(TransactionCreationDto dto) {
    UUID categoryId = dto.categoryId() != null
        ? dto.categoryId()
        : getDefaultCategoryId(); // "Others" category

    validateCategoryExists(categoryId); // FR-015
    // ...
}

private UUID getDefaultCategoryId() {
    return categoryRepository.findByName("Others")
        .orElseThrow(() -> new IllegalStateException("Default 'Others' category not found"))
        .getId();
}
```

### Category Filtering Results

**Decision**: Return HTTP 200 OK with empty array `[]` when category has no transactions (clarification answer: Option B).

**Rationale**: Standard REST practice, successful query with no results (semantically correct).

**Implementation**:
```java
@GetMapping("/v1/transactions")
public ResponseEntity<List<TransactionDto>> filterByCategory(
    @RequestParam UUID categoryId
) {
    // Validate category exists
    if (!categoryRepository.existsById(categoryId)) {
        throw new NotFoundException("Category not found");
    }

    List<Transaction> transactions = transactionRepository.findByCategoryId(categoryId);
    // Returns empty list if no transactions (maps to HTTP 200 with [])
    return ResponseEntity.ok(transactions.stream().map(mapper::toDto).toList());
}
```

---

## 5. Transaction Amount Validation

### Negative Amount Handling

**Decision**: Reject negative or zero amounts with validation error (clarification answer: Option A).

**Rationale**: Transaction type (EXPENSE/INCOME) explicitly determines direction; negative amounts would create confusion.

**Implementation**:
```java
public record TransactionCreationDto(
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 9, fraction = 2, message = "Amount must have max 9 integer and 2 decimal digits")
    BigDecimal amount,

    @NotNull
    TransactionType transactionType,

    // ...
) {}
```

**Validation Error Response**: HTTP 400 Bad Request with field-level error details.

---

## 6. API Migration Strategy

### Endpoint Renaming Approach

**Decision**: Replace `/v1/expenses` with `/v1/transactions` immediately, no backward compatibility (clarification answer: Option C).

**Rationale**:
- Breaking change acknowledged in requirements (FR-031)
- Development environment allows clean breaks
- All API clients update simultaneously (no multi-version support needed)

**Migration Steps**:
1. **Create new endpoints** `/v1/transactions/*` with Transaction entities
2. **Test new endpoints** with integration tests
3. **Delete old endpoints** `/v1/expenses/*`
4. **Old Endpoints**: Return HTTP 404 Not Found after deletion

**Alternative Considered: API Versioning (/v2/transactions)**
- Would maintain backward compatibility longer
- **Rejected because**: User preference for clean break, simpler implementation

---

## 7. JPA Entity Design Decisions

### Transaction Entity Structure

**Key Design Choices**:

1. **Mutable Class** (not record) for JPA compatibility
2. **UUID for ID** (existing pattern, good for distributed systems)
3. **@Version field** for optimistic locking (existing pattern)
4. **Factory method** `Transaction.create()` for creation (encapsulates invariants)
5. **Audit timestamps** (createdAt, updatedAt) auto-managed by JPA

```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 32, nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_stability", length = 32, nullable = true)
    private TransactionStability transactionStability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    // Factory method, getters, equals/hashCode based on ID
}
```

### Category Entity Structure

```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean isPredefined = false;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    // Factory methods
    public static Category createPredefined(String name) {
        return new Category(null, name, true, ZonedDateTime.now(ZoneOffset.UTC), ZonedDateTime.now(ZoneOffset.UTC));
    }

    public static Category createCustom(String name) {
        return new Category(null, name, false, ZonedDateTime.now(ZoneOffset.UTC), ZonedDateTime.now(ZoneOffset.UTC));
    }
}
```

---

## 8. Performance Considerations

### Indexing Strategy

**Required Indexes** (Hibernate auto-creates for @ManyToOne):
- `category_id` on transactions (foreign key index)
- Composite index on `(date, category_id)` for common filter queries

**Additional Indexes** (manually created if needed):
```sql
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

**Rationale**:
- `date` index: Support reverse chronological ordering (existing requirement)
- `category_id` index: Enable fast category filtering (auto-created by FK)
- `transaction_type` index: Support filtering by EXPENSE/INCOME

### Query Patterns

**Expected Common Queries**:
1. List transactions by date range + category
2. Calculate totals by transaction type (EXPENSE vs INCOME)
3. Filter by expense type (FIXED vs VARIABLE)
4. List all categories with transaction counts

**Optimization**: Use `@EntityGraph` or fetch joins to avoid N+1 queries when loading transactions with categories.

```java
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @EntityGraph(attributePaths = {"category"})
    List<Transaction> findAllByOrderByDateDesc();
}
```

---

## Sources

### Enum Handling
- [Persisting Enums in JPA | Baeldung](https://www.baeldung.com/jpa-persisting-enums-in-jpa)
- [The best way to map an Enum Type with JPA](https://vladmihalcea.com/the-best-way-to-map-an-enum-type-with-jpa-and-hibernate/)
- [Enum Mappings with Hibernate - Complete Guide](https://thorben-janssen.com/hibernate-enum-mappings/)
- [Validations for Enum Types | Baeldung](https://www.baeldung.com/javax-validations-enums)

### JPA Best Practices
- [Best Practices for Many-To-One and One-To-Many Association Mappings](https://thoughts-on-java.org/best-practices-for-many-to-one-and-one-to-many-association-mappings/)
- [How to implement equals and hashCode using the JPA entity identifier](https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/)