# Data Model: Transaction Categories and Types

**Feature**: 002-transaction-categories
**Date**: 2026-01-19
**Status**: Complete

## Overview

This document defines the data model for transaction categories, transaction types (EXPENSE/INCOME), and expense types (FIXED/VARIABLE). The model extends the existing expense tracking system to support richer classification and reporting capabilities.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Transaction                          │
├─────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                                │
│ description: String(500) NOT NULL                            │
│ amount: BigDecimal(11,2) NOT NULL                            │
│ currency: String(3) NOT NULL                                 │
│ date: ZonedDateTime NOT NULL                                 │
│ transactionType: TransactionType(32) NOT NULL                │
│ transactionStability: TransactionStability(32) NOT NULL      │
│ categoryId: UUID (FK) NOT NULL ───────┐                     │
│ version: Integer NOT NULL              │                     │
│ createdAt: ZonedDateTime NOT NULL      │                     │
│ updatedAt: ZonedDateTime NOT NULL      │                     │
└────────────────────────────────────────┼─────────────────────┘
                                         │
                                         │ Many-to-One
                                         │
                                         ▼
                        ┌────────────────────────────────────┐
                        │           Category                 │
                        ├────────────────────────────────────┤
                        │ id: UUID (PK)                      │
                        │ name: String(100) NOT NULL UNIQUE  │
                        │ isPredefined: Boolean NOT NULL     │
                        │ createdAt: ZonedDateTime NOT NULL  │
                        │ updatedAt: ZonedDateTime NOT NULL  │
                        └────────────────────────────────────┘

┌───────────────────────────┐     ┌─────────────────────────────────┐
│   TransactionType (Enum)  │     │ TransactionStability (Enum)     │
├───────────────────────────┤     ├─────────────────────────────────┤
│ EXPENSE                   │     │ FIXED                           │
│ INCOME                    │     │ VARIABLE                        │
└───────────────────────────┘     └─────────────────────────────────┘
```

## Entities

### Transaction (formerly Expense)

**Purpose**: Represents a financial transaction (income or expense) with classification and categorization.

**Table Name**: `transactions` (renamed from `expenses`)

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, AUTO-GENERATED | Unique identifier |
| `description` | VARCHAR(500) | NOT NULL | Transaction description |
| `amount` | DECIMAL(11,2) | NOT NULL, > 0 | Positive amount (type determines direction) |
| `currency` | VARCHAR(3) | NOT NULL | ISO 4217 currency code |
| `date` | TIMESTAMP WITH TIME ZONE | NOT NULL, <= NOW | Transaction date (past or present) |
| `transactionType` | VARCHAR(32) | NOT NULL | EXPENSE or INCOME |
| `transactionStability` | VARCHAR(32) | NOT NULL | FIXED or VARIABLE (applies to both types) |
| `categoryId` | UUID | NOT NULL, FK → categories.id | Foreign key to category |
| `version` | INTEGER | NOT NULL, DEFAULT 0 | Optimistic locking version |
| `createdAt` | TIMESTAMP WITH TIME ZONE | NOT NULL, IMMUTABLE | Record creation timestamp (UTC) |
| `updatedAt` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last update timestamp (UTC) |

**Indexes**:
```sql
CREATE INDEX idx_transactions_date ON transactions(date DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

**Validation Rules** (enforced at application layer):
- `amount` must be > 0.00 (negative amounts rejected)
- `amount` max 9 integer digits, 2 decimal places
- `currency` must be valid ISO 4217 code (validated via `@ValidCurrency`)
- `date` cannot be in the future (validated via `@PastOrPresent`)
- `transactionType` required on create/update
- `transactionStability` required on create/update (applies to all transaction types)
- `categoryId` must reference existing category

**Business Rules**:
- Version increments on each update (optimistic locking)
- `createdAt` set once on creation, never updated
- `updatedAt` set on creation and every update
- All timestamps stored in UTC, converted from client timezone

**State Transitions**:
```
CREATE → [amount, type, category required]
UPDATE → [preserves id, createdAt; version incremented]
DELETE → [soft delete not supported, hard delete only]
```

---

### Category

**Purpose**: Classification for transactions (e.g., "Food & Drinks", "Bank", "Subscriptions").

**Table Name**: `categories`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, AUTO-GENERATED | Unique identifier |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE (case-insensitive) | Category name |
| `isPredefined` | BOOLEAN | NOT NULL, DEFAULT false | True for system-seeded categories |
| `createdAt` | TIMESTAMP WITH TIME ZONE | NOT NULL, IMMUTABLE | Record creation timestamp (UTC) |
| `updatedAt` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last update timestamp (UTC) |

**Indexes**:
```sql
-- Case-insensitive uniqueness enforced at application level (H2 limitation)
-- Foreign key index auto-created by JPA on transactions.category_id
```

**Validation Rules**:
- `name` must be unique (case-insensitive comparison)
- `name` max 100 characters
- `name` cannot be blank/empty

**Business Rules**:
- 14 predefined categories seeded on first startup
- Predefined categories can be renamed by users (no special protection)
- Categories cannot be deleted if associated with any transactions
- Categories with zero transactions can be deleted freely

**Predefined Categories** (seeded via `CommandLineRunner`):
1. Office Renting
2. Public Transport
3. Bank
4. Car Maintenance
5. Food & Drinks
6. Subscriptions
7. Supermarket
8. Tolls
9. Gas
10. Sport
11. Gifts
12. ATM
13. Video & Films
14. Transfers
15. Others (default category)

---

## Enumerations

### TransactionType

**Purpose**: Distinguishes money coming in (income) from money going out (expenses).

**Storage**: `VARCHAR(32)` via `@Enumerated(EnumType.STRING)`

**Values**:
- `EXPENSE`: Money going out (e.g., groceries, rent, subscriptions)
- `INCOME`: Money coming in (e.g., salary, refunds, gifts)

**Usage**:
- Required for all transactions
- Determines semantic meaning of amount (amount always positive)
- Used for filtering and summary calculations

**Future Extensibility**: Can add `TRANSFER` without breaking existing data (stored as STRING)

---

### TransactionStability

**Purpose**: Classifies transactions as recurring/predictable (FIXED) or one-time/variable (VARIABLE).

**Storage**: `VARCHAR(32)` via `@Enumerated(EnumType.STRING)`

**Values**:
- `FIXED`: Recurring, predictable transactions (e.g., salary, subscriptions, insurance, rent)
- `VARIABLE`: One-time, unpredictable transactions (e.g., freelance income, groceries, gas, entertainment)

**Usage**:
- **Applies to both EXPENSE and INCOME transactions**
- Required for all transactions (not nullable)
- Defaults to VARIABLE if not specified
- Used for budget planning, cash flow prediction, and financial categorization

**Examples**:
- EXPENSE + FIXED: Monthly subscription, rent, insurance
- EXPENSE + VARIABLE: Groceries, gas, one-time purchases
- INCOME + FIXED: Salary, pension, regular dividends
- INCOME + VARIABLE: Freelance payments, bonuses, gifts

---

## Relationships

### Transaction → Category (Many-to-One)

**Cardinality**: Many transactions → One category

**Foreign Key**: `transactions.category_id` → `categories.id`

**Fetch Strategy**: `LAZY` (avoid N+1 queries with `@EntityGraph`)

**Cascade**: None (categories managed independently)

**Deletion Rules**:
- **Category with transactions**: Cannot be deleted (409 Conflict)
- **Category deleted**: Not applicable (blocked)
- **Transaction deleted**: No impact on category

**Default Behavior**:
- If no category specified: Use "Others" category (looked up by name)
- If invalid category ID provided: 400 Bad Request (validation error)

---

## Migration Strategy from Expense to Transaction

### Approach: In-Place Rename (Option 1)

**Steps**:

1. **Rename Entity Class**:
   ```java
   // Before
   @Entity
   @Table(name = "expenses")
   public class Expense { ... }

   // After
   @Entity
   @Table(name = "transactions")
   public class Transaction { ... }
   ```

2. **Hibernate DDL Update**:
   - Hibernate detects table name change
   - Renames `expenses` table to `transactions` automatically (H2 DDL support)
   - No data loss

3. **Add New Columns**:
   ```java
   @Enumerated(EnumType.STRING)
   @Column(name = "transaction_type", length = 32, nullable = true) // Nullable initially
   private TransactionType transactionType;

   @Enumerated(EnumType.STRING)
   @Column(name = "transaction_stability", length = 32, nullable = true) // Nullable initially
   private TransactionStability transactionStability;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "category_id", nullable = true) // Nullable initially
   private Category category;
   ```

4. **Seed Categories** (one-time on startup):
   ```java
   @Component
   public class CategorySeeder implements CommandLineRunner {
       public void run(String... args) {
           if (categoryRepository.count() == 0) {
               // Insert 14 predefined categories
           }
       }
   }
   ```

5. **Set Default Values for Existing Records** (one-time migration):
   ```java
   @Component
   public class TransactionMigration implements CommandLineRunner {
       @Transactional
       public void run(String... args) {
           UUID othersId = categoryRepository.findByName("Others").get().getId();

           List<Transaction> unmigrated = transactionRepository
               .findByTransactionTypeIsNull();

           for (Transaction t : unmigrated) {
               t.setTransactionType(TransactionType.EXPENSE);
               t.setTransactionStability(TransactionStability.VARIABLE);
               t.setCategory(categoryRepository.findById(othersId).get());
           }

           transactionRepository.saveAll(unmigrated);
       }
   }
   ```

6. **Make Columns Non-Nullable** (after migration complete):
   ```java
   @Column(name = "transaction_type", length = 32, nullable = false)
   private TransactionType transactionType;

   @JoinColumn(name = "category_id", nullable = false)
   private Category category;
   ```

---

## Data Integrity Constraints

### Database Level

1. **Primary Keys**: All entities use UUID
2. **Foreign Keys**: `transactions.category_id` → `categories.id`
3. **Optimistic Locking**: `version` column on Transaction

### Application Level

1. **Unique Category Names**: Case-insensitive check before create/update
2. **Positive Amounts**: Validated via `@DecimalMin("0.01")`
3. **Valid Currency Codes**: Custom `@ValidCurrency` annotation
4. **Required TransactionStability**: Must be FIXED or VARIABLE for all transactions
5. **Category Existence**: Check before linking to transaction

---

## Query Patterns

### Common Queries

**1. List all transactions by date (reverse chronological)**:
```java
List<Transaction> findAllByOrderByDateDesc();
```

**2. Filter transactions by category**:
```java
@EntityGraph(attributePaths = {"category"})
List<Transaction> findByCategoryIdOrderByDateDesc(UUID categoryId);
```

**3. Filter by transaction type**:
```java
List<Transaction> findByTransactionTypeOrderByDateDesc(TransactionType type);
```

**4. Filter EXPENSE transactions by expense type**:
```java
List<Transaction> findByTransactionTypeAndTransactionStabilityOrderByDateDesc(
    TransactionType type,
    TransactionStability stability
);
```

**5. Calculate totals by transaction type**:
```java
@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = :type")
BigDecimal sumAmountByType(@Param("type") TransactionType type);
```

**6. Count transactions for a category** (for deletion validation):
```java
long countByCategoryId(UUID categoryId);
```

**7. Find category by name** (case-insensitive):
```java
Optional<Category> findByNameIgnoreCase(String name);
boolean existsByNameIgnoreCase(String name);
```

---

## Example Data

### Transaction Examples

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "description": "REGUS BUSINESS CENTRE",
    "amount": 30.75,
    "currency": "EUR",
    "date": "2026-01-12T00:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE",
    "category": {
      "id": "...",
      "name": "Office Renting"
    },
    "version": 0,
    "createdAt": "2026-01-12T10:30:00Z",
    "updatedAt": "2026-01-12T10:30:00Z"
  },
  {
    "id": "...",
    "description": "Salary Deposit",
    "amount": 3000.00,
    "currency": "EUR",
    "date": "2026-01-01T00:00:00Z",
    "transactionType": "INCOME",
    "transactionStability": "FIXED",
    "category": {
      "id": "...",
      "name": "Transfers"
    },
    "version": 0,
    "createdAt": "2026-01-01T09:00:00Z",
    "updatedAt": "2026-01-01T09:00:00Z"
  }
]
```

### Category Examples

```json
[
  {
    "id": "...",
    "name": "Food & Drinks",
    "isPredefined": true,
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-01-01T00:00:00Z"
  },
  {
    "id": "...",
    "name": "Medical Expenses",
    "isPredefined": false,
    "createdAt": "2026-01-15T10:30:00Z",
    "updatedAt": "2026-01-15T10:30:00Z"
  }
]
```

---

## Schema Evolution

### Current Version (After Migration)

```sql
-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_predefined BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Transactions table (renamed from expenses)
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(11, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    transaction_stability VARCHAR(32) NOT NULL,
    category_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Indexes
CREATE INDEX idx_transactions_date ON transactions(date DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

---

## Performance Considerations

### Read Operations
- **Category filtering**: Indexed on `category_id` (FK index)
- **Date ordering**: Indexed on `date` (common query pattern)
- **Type filtering**: Indexed on `transaction_type`
- **N+1 Prevention**: Use `@EntityGraph(attributePaths = {"category"})` for joins

### Write Operations
- **Optimistic locking**: Version check prevents lost updates
- **Index maintenance**: Three indexes updated per transaction write
- **Expected load**: <100 transactions/day (acceptable overhead)

### Storage Estimates
- **Transaction**: ~200 bytes/record (with indexes)
- **Category**: ~100 bytes/record
- **1 year of data** (~30k transactions): ~6 MB

---

## Future Extensibility

### Potential Enhancements (Out of Scope)

1. **Subcategories**: Hierarchical category structure
2. **Tags**: Many-to-many relationship for flexible classification
3. **Recurring Transactions**: Template system for FIXED expenses
4. **Budgets**: Category-based spending limits
5. **Multi-currency**: Automatic conversion to base currency
6. **Attachments**: Receipts/documents linked to transactions

### Schema Ready For:
- Adding new transaction types (e.g., TRANSFER) via enum extension
- Adding new expense types (e.g., EMERGENCY) via enum extension
- Additional transaction fields (e.g., notes, tags) as new columns
