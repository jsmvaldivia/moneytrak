# Feature Specification: Transaction Categories and Types

**Feature Branch**: `002-transaction-categories`
**Created**: 2026-01-19
**Status**: Draft
**Input**: User description: "Enhance MoneyTrak to support: 1. Categories - Add category management system with predefined categories (Office Renting, Public Transport, Bank, Car Maintenance, Food & Drinks, Subscriptions, Supermarket, Tolls, Gas, Sport, Gifts, ATM, Video & Films, Transfers, Others) 2. Transaction Stability enum - Add FIXED (recurring, predictable) vs VARIABLE (one-time, unpredictable) classification for all transaction types 3. Transaction Type - Support both EXPENSE (negative) and INCOME (positive) transactions 4. Rename domain model from Expense to Transaction to reflect broader scope 5. Allow positive amounts with explicit transaction type 6. Link transactions to categories 7. Maintain backward compatibility with existing expense data during migration"

## Clarifications

### Session 2026-01-19

- Q: When a user provides a negative amount (e.g., -30.75), how should the system respond? → A: Reject with validation error requiring positive amounts only
- Q: Can users rename or modify the 14 predefined categories (e.g., "Bank" → "Banking", "Gas" → "Fuel")? → A: Yes, all categories (predefined and custom) can be renamed by users
- Q: When a user tries to create a category with a name that already exists (case-insensitive), what HTTP status code should the system return? → A: 409 Conflict (resource conflict, same as optimistic locking violations)
- Q: When filtering transactions by a category that exists but has no associated transactions, what should the system return? → A: HTTP 200 OK with empty array []
- Q: How should the system handle existing `/v1/expenses` API endpoints after renaming to `/v1/transactions`? → A: Replace immediately with `/v1/transactions`, no backward compatibility

### Session 2026-01-22

- Q: When one user modifies a predefined category name (e.g., "Gas" → "Fuel"), how does this affect other users in a multi-user system? → A: The change applies globally to all users (any user can rename predefined categories, affecting everyone)
- Q: Does transaction stability (FIXED vs VARIABLE) apply only to EXPENSE transactions, or to both INCOME and EXPENSE? → A: Transaction stability applies to both INCOME and EXPENSE transactions; default is VARIABLE for all transaction types
- Q: What happens when a transaction is created without specifying a category? → A: Automatically assign default category "Others" (consistent with FR-014)
- Q: What HTTP status code should the system return when attempting to delete a category that has linked transactions? → A: HTTP 409 Conflict (consistent with other constraint violations like duplicate names)
- Q: What happens when updating a transaction's category to a non-existent category ID? → A: HTTP 404 Not Found (category resource doesn't exist)

## User Scenarios & Testing

### User Story 1 - Manage Transaction Categories (Priority: P1) 🎯 MVP

Users need to organize their financial transactions into meaningful categories to understand spending patterns and generate useful reports.

**Why this priority**: Categories are foundational for all other features. Without categories, users cannot properly classify transactions, making transaction types and expense classifications less valuable. This is the core organizational structure.

**Independent Test**: Can be fully tested by creating, reading, updating, and deleting categories through the API, and verifies that predefined categories exist on system initialization.

**Acceptance Scenarios**:

1. **Given** the system starts fresh, **When** user requests all categories, **Then** system returns 14 predefined categories (Office Renting, Public Transport, Bank, Car Maintenance, Food & Drinks, Subscriptions, Supermarket, Tolls, Gas, Sport, Gifts, ATM, Video & Films, Transfers, Others)
2. **Given** predefined categories exist, **When** user creates a new custom category "Medical Expenses", **Then** system persists the category and returns success with category ID
3. **Given** a category exists, **When** user retrieves category by ID, **Then** system returns the complete category details
4. **Given** a predefined category "Gas" exists, **When** user updates the name to "Fuel", **Then** system persists the change and returns updated category
5. **Given** a custom category exists, **When** user updates the category name, **Then** system persists the change and returns updated category
6. **Given** a category exists with no linked transactions, **When** user deletes the category, **Then** system removes the category permanently
7. **Given** a category exists with linked transactions, **When** user attempts to delete the category, **Then** system prevents deletion and returns HTTP 409 Conflict with error message

---

### User Story 2 - Link Transactions to Categories (Priority: P2)

Users need to assign categories to their transactions when recording financial activity, enabling organized tracking and categorized reporting.

**Why this priority**: Once categories exist, the immediate value comes from linking transactions to them. This enables users to organize existing and new transactions, directly supporting real-world use cases like the sample data provided.

**Independent Test**: Can be tested by creating transactions with category assignments, updating transaction categories, and retrieving transactions with their associated category information.

**Acceptance Scenarios**:

1. **Given** categories exist, **When** user creates a transaction with category "Food & Drinks" and amount 23.70, **Then** system links transaction to category and persists both
2. **Given** an existing transaction without category, **When** user updates transaction to add category "Bank", **Then** system updates the transaction-category link
3. **Given** a transaction with category "Tolls", **When** user updates the category to "Gas", **Then** system updates the category link and preserves transaction integrity
4. **Given** transactions with various categories, **When** user retrieves all transactions, **Then** system returns transactions with their associated category names
5. **Given** a specific category "Supermarket", **When** user requests all transactions for that category, **Then** system returns only transactions linked to "Supermarket"

---

### User Story 3 - Classify Transaction Types (Priority: P3)

Users need to distinguish between money coming in (income) and money going out (expenses) to accurately track their financial position and generate income vs expense reports.

**Why this priority**: Transaction type classification provides essential financial clarity. After organizing transactions by category, users need to differentiate income from expenses for accurate financial reporting and budget tracking.

**Independent Test**: Can be tested by creating transactions with EXPENSE and INCOME types, verifying that amounts remain positive regardless of type, and generating summaries that correctly separate income from expenses.

**Acceptance Scenarios**:

1. **Given** user records a transaction, **When** user sets type as EXPENSE with amount 30.75 and category "Office Renting", **Then** system stores transaction type as EXPENSE with positive amount
2. **Given** user records a transaction, **When** user sets type as INCOME with amount 23.70 and category "Food & Drinks", **Then** system stores transaction type as INCOME with positive amount
3. **Given** existing transactions with different types, **When** user requests expense summary, **Then** system calculates total from EXPENSE transactions only
4. **Given** existing transactions with different types, **When** user requests income summary, **Then** system calculates total from INCOME transactions only
5. **Given** a transaction exists, **When** user updates transaction type from EXPENSE to INCOME, **Then** system updates the type while preserving the positive amount

---

### User Story 4 - Classify Transaction Stability (Priority: P4)

Users need to distinguish between fixed recurring transactions (predictable, like subscriptions or salary) and variable one-time transactions (unpredictable, like groceries or gifts) to better plan budgets and identify saving opportunities.

**Why this priority**: Transaction stability classification adds analytical value on top of transaction categorization and type. It helps with budget planning but is not essential for basic transaction tracking.

**Independent Test**: Can be tested by creating transactions with FIXED and VARIABLE stability for both INCOME and EXPENSE types, filtering transactions by stability, and verifying that the classification persists correctly.

**Acceptance Scenarios**:

1. **Given** user records an EXPENSE transaction, **When** user sets transaction stability as FIXED with category "Subscriptions", **Then** system stores transaction stability as FIXED
2. **Given** user records an EXPENSE transaction, **When** user sets transaction stability as VARIABLE with category "Gas", **Then** system stores transaction stability as VARIABLE
3. **Given** user records an INCOME transaction, **When** user sets transaction stability as FIXED with category "Transfers", **Then** system stores transaction stability as FIXED (applies to income too)
4. **Given** existing transactions, **When** user filters by FIXED stability, **Then** system returns only transactions marked as FIXED (both INCOME and EXPENSE)
5. **Given** a transaction with stability FIXED, **When** user updates to VARIABLE, **Then** system updates the transaction stability classification

---

### User Story 5 - Migrate from Expense to Transaction Model (Priority: P5)

The system needs to rename the domain model from "Expense" to "Transaction" to reflect the broader scope of tracking both income and expenses, while maintaining backward compatibility with existing data.

**Why this priority**: This is primarily a technical refactoring that supports the new features. It should be done last to minimize disruption, after all new features are validated and working.

**Independent Test**: Can be tested by verifying that existing expense records are accessible as transactions with default values (type: EXPENSE, expense type: VARIABLE, category: Others), all existing API endpoints work with new naming, and no data is lost during migration.

**Acceptance Scenarios**:

1. **Given** existing expense records in the system, **When** migration runs, **Then** all expenses become transactions with type EXPENSE, transaction stability VARIABLE, and category "Others"
2. **Given** migration is complete, **When** user retrieves old expense by ID using `/v1/transactions/{id}`, **Then** system returns it as a transaction with preserved amount, date, description, and currency
3. **Given** migrated transactions, **When** user updates a migrated transaction, **Then** system allows updating category, transaction type, and transaction stability
4. **Given** migration is complete, **When** client accesses old `/v1/expenses` endpoints, **Then** system returns HTTP 404 Not Found (endpoints no longer exist)

---

### Edge Cases

- **Duplicate category names**: System returns HTTP 409 Conflict when creating or updating a category with a name that already exists (case-insensitive comparison)
- **Delete category with transactions**: System returns HTTP 409 Conflict when attempting to delete a category that has associated transactions
- **Missing category on creation**: System automatically assigns default category "Others" when no category is specified during transaction creation
- **Invalid category reference**: System returns HTTP 404 Not Found when creating or updating a transaction with a non-existent category ID
- **Missing transaction stability**: System defaults to VARIABLE when transaction stability is not specified during creation
- How does the system handle migration if existing expense records have NULL or invalid currency codes?
- **Empty category filter results**: System returns HTTP 200 OK with empty array [] when filtering by a category that has no associated transactions
- How does the system handle concurrent updates to the same category name?
- **Negative amounts**: System rejects with validation error when negative or zero amount is provided (amounts must be positive only)

## Requirements

### Functional Requirements

#### Category Management

- **FR-001**: System MUST provide 14 predefined categories on initialization: Office Renting, Public Transport, Bank, Car Maintenance, Food & Drinks, Subscriptions, Supermarket, Tolls, Gas, Sport, Gifts, ATM, Video & Films, Transfers, Others
- **FR-002**: System MUST allow users to create custom categories with unique names
- **FR-003**: System MUST allow users to retrieve all categories (predefined and custom)
- **FR-004**: System MUST allow users to retrieve a single category by ID
- **FR-005**: System MUST allow users to update category names for both predefined and custom categories (changes apply globally to all users)
- **FR-006**: System MUST prevent deletion of categories that have associated transactions and return HTTP 409 Conflict
- **FR-007**: System MUST allow deletion of categories with no associated transactions
- **FR-008**: System MUST enforce unique category names (case-insensitive)
- **FR-008a**: System MUST return HTTP 409 Conflict when attempting to create or update a category with a duplicate name
- **FR-009**: System MUST validate category names with maximum length of 100 characters

#### Transaction-Category Linking

- **FR-010**: System MUST allow users to assign a category when creating a transaction
- **FR-011**: System MUST allow users to update the category of an existing transaction
- **FR-012**: System MUST return category information when retrieving transactions
- **FR-013**: System MUST allow filtering transactions by category ID
- **FR-013a**: System MUST return HTTP 200 OK with empty array when filtering by a category that has no associated transactions
- **FR-014**: System MUST assign default category "Others" when no category is specified
- **FR-015**: System MUST validate that assigned category exists before linking to transaction and return HTTP 404 Not Found if category does not exist

#### Transaction Type Classification

- **FR-016**: System MUST support TransactionType enum with values: EXPENSE, INCOME
- **FR-017**: System MUST require transaction type when creating transactions
- **FR-018**: System MUST store amounts as positive values regardless of transaction type
- **FR-018a**: System MUST reject transaction creation/update with validation error when amount is negative or zero
- **FR-019**: System MUST allow filtering transactions by transaction type
- **FR-020**: System MUST allow updating transaction type of existing transactions
- **FR-021**: System MUST calculate expense totals from transactions with type EXPENSE
- **FR-022**: System MUST calculate income totals from transactions with type INCOME

#### Transaction Stability Classification

- **FR-023**: System MUST support TransactionStability enum with values: FIXED, VARIABLE
- **FR-024**: System MUST allow setting transaction stability for all transactions (both EXPENSE and INCOME types)
- **FR-025**: System MUST default transaction stability to VARIABLE if not specified for any transaction type
- **FR-026**: System MUST allow filtering transactions by stability (FIXED or VARIABLE) regardless of transaction type
- **FR-027**: System MUST allow updating transaction stability for existing transactions

#### Migration and Backward Compatibility

- **FR-028**: System MUST migrate existing expense records to transaction records preserving: amount, description, currency, date, version, createdAt, updatedAt, and ID
- **FR-029**: System MUST set default values for migrated records: TransactionType = EXPENSE, TransactionStability = VARIABLE, Category = "Others"
- **FR-030**: System MUST maintain data integrity during migration (no data loss)
- **FR-031**: System MUST replace API endpoints from `/v1/expenses` to `/v1/transactions` with no backward compatibility (breaking change)
- **FR-032**: System MUST rename entity classes from Expense to Transaction
- **FR-033**: System MUST rename database tables from `expenses` to `transactions`

### Key Entities

- **Category**: Represents a classification for transactions (e.g., "Food & Drinks", "Bank", "Gas"). Contains unique name, ID, predefined flag (to distinguish system vs user-created), timestamps for creation/update.

- **Transaction** (formerly Expense): Represents a financial transaction (income or expense). Contains description, positive amount, currency code, transaction date, transaction type (EXPENSE/INCOME), transaction stability (FIXED/VARIABLE, applicable to all transaction types), link to category, version for optimistic locking, and audit timestamps.

- **TransactionType**: Enumeration with values EXPENSE (money going out) and INCOME (money coming in).

- **TransactionStability**: Enumeration with values FIXED (recurring, predictable) and VARIABLE (one-time, unpredictable). Applicable to both EXPENSE and INCOME transaction types. Default value is VARIABLE.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can create and manage custom categories in under 30 seconds per category
- **SC-002**: Users can assign categories to transactions during creation without additional steps
- **SC-003**: Users can filter their transaction history by category and see results in under 2 seconds
- **SC-004**: Users can distinguish between income and expenses by viewing transaction type
- **SC-005**: Users can classify all transactions (both INCOME and EXPENSE) as FIXED or VARIABLE to enable budget planning
- **SC-006**: System successfully migrates 100% of existing expense records to transaction model with zero data loss
- **SC-007**: Users can complete the same workflows after migration without retraining or documentation changes
- **SC-008**: Category-based transaction filtering returns accurate results matching the assigned categories
- **SC-009**: Transaction type summaries (total income vs total expenses) calculate correctly with 100% accuracy

## Assumptions

- Categories are global (not user-specific) for this version; any user can create, rename, or delete categories, affecting all users
- The 14 predefined categories cover common use cases for personal finance tracking
- Users prefer category names in English; localization is not required initially
- Transaction stability classification (FIXED/VARIABLE) is manually assigned by users, not automatically detected, and applies to both INCOME and EXPENSE transactions
- Migration from Expense to Transaction model can be performed during a maintenance window with coordinated API client updates
- All API clients MUST be updated to use new `/v1/transactions` endpoints before migration (breaking change)
- Amount values remain positive with transaction type providing semantic meaning
- Default category "Others" is acceptable for uncategorized transactions
- Category deletion is permanently blocked once any transaction links to it (soft delete not required)
