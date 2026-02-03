# Quickstart: Transaction Categories and Types

**Feature**: 002-transaction-categories
**Date**: 2026-01-19

## Overview

This guide provides quick-start examples for testing the new transaction categories and types API endpoints using `curl`.

## Prerequisites

- Application running on `http://localhost:8080`
- `curl` command available
- `jq` for JSON formatting (optional but recommended)

## Start the Application

```bash
./mvnw spring-boot:run
```

Wait for the application to start. You should see:
```
Started MoneytrakApplication in X.XXX seconds
```

---

## Category Management

### 1. List All Categories

Get all categories (14 predefined + any custom):

```bash
curl -X GET http://localhost:8080/v1/categories | jq
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Office Renting",
    "isPredefined": true,
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-01-01T00:00:00Z"
  },
  {
    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "name": "Public Transport",
    "isPredefined": true,
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-01-01T00:00:00Z"
  },
  // ... 12 more predefined categories
]
```

### 2. Create a Custom Category

Create a new user-defined category:

```bash
curl -X POST http://localhost:8080/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Medical Expenses"
  }' | jq
```

**Expected Response** (201 Created):
```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "name": "Medical Expenses",
  "isPredefined": false,
  "createdAt": "2026-01-19T10:30:00Z",
  "updatedAt": "2026-01-19T10:30:00Z"
}
```

**Location Header**: `/v1/categories/7c9e6679-7425-40de-944b-e07fc1f90ae7`

### 3. Get Category by ID

Retrieve a specific category:

```bash
curl -X GET http://localhost:8080/v1/categories/7c9e6679-7425-40de-944b-e07fc1f90ae7 | jq
```

### 4. Update Category Name

Rename a category (works for both predefined and custom, requires version):

```bash
curl -X PUT http://localhost:8080/v1/categories/7c9e6679-7425-40de-944b-e07fc1f90ae7 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Healthcare",
    "version": 0
  }' | jq
```

**Expected Response** (200 OK):
```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "name": "Healthcare",
  "isPredefined": false,
  "createdAt": "2026-01-19T10:30:00Z",
  "updatedAt": "2026-01-19T10:35:00Z"
}
```

### 5. Delete Category

Delete a category (only if no transactions associated):

```bash
curl -X DELETE http://localhost:8080/v1/categories/7c9e6679-7425-40de-944b-e07fc1f90ae7 -v
```

**Expected Response** (204 No Content) if successful.

**Error Response** (409 Conflict) if category has transactions:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete category with 5 associated transactions",
  "timestamp": "2026-01-19T10:40:00Z"
}
```

### 6. Duplicate Category Name

Attempt to create a category with existing name:

```bash
curl -X POST http://localhost:8080/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Medical Expenses"
  }' | jq
```

**Expected Response** (409 Conflict):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Category with name 'Medical Expenses' already exists",
  "timestamp": "2026-01-19T10:45:00Z"
}
```

---

## Transaction Management

### 1. Create a Variable Expense

Record a one-time expense (groceries):

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Groceries at Continente",
    "amount": 45.30,
    "currency": "EUR",
    "date": "2026-01-19T10:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE",
    "categoryId": "550e8400-e29b-41d4-a716-446655440000"
  }' | jq
```

**Expected Response** (201 Created):
```json
{
  "id": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "description": "Groceries at Continente",
  "amount": 45.30,
  "currency": "EUR",
  "date": "2026-01-19T10:00:00Z",
  "transactionType": "EXPENSE",
  "transactionStability": "VARIABLE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440000",
  "categoryName": "Supermarket",
  "version": 0,
  "createdAt": "2026-01-19T10:30:00Z",
  "updatedAt": "2026-01-19T10:30:00Z"
}
```

### 2. Create a Fixed Expense

Record a recurring expense (subscription):

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Netflix Monthly Subscription",
    "amount": 12.99,
    "currency": "EUR",
    "date": "2026-01-01T00:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "FIXED",
    "categoryId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
  }' | jq
```

### 3. Create Fixed Income

Record recurring income (salary):

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Monthly Salary",
    "amount": 3000.00,
    "currency": "EUR",
    "date": "2026-01-01T00:00:00Z",
    "transactionType": "INCOME",
    "transactionStability": "FIXED"
  }' | jq
```

**Note**: No `categoryId` provided, defaults to "Others" category.

### 4. Create Variable Income

Record one-time income (freelance payment):

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Freelance Project Payment",
    "amount": 500.00,
    "currency": "EUR",
    "date": "2026-01-15T00:00:00Z",
    "transactionType": "INCOME",
    "transactionStability": "VARIABLE",
    "categoryId": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
  }' | jq
```

### 5. List All Transactions

Get all transactions ordered by date (most recent first):

```bash
curl -X GET http://localhost:8080/v1/transactions | jq
```

### 6. Filter Transactions by Category

Get all transactions for "Supermarket" category:

```bash
CATEGORY_ID="550e8400-e29b-41d4-a716-446655440000"
curl -X GET "http://localhost:8080/v1/transactions?categoryId=${CATEGORY_ID}" | jq
```

**Expected Response**: Array of transactions (or empty array `[]` if no matches).

### 7. Get Transaction Summaries

Get total of all expenses:

```bash
curl -X GET "http://localhost:8080/v1/transactions/summary/expenses" | jq
```

**Expected Response**: `3500.50` (sum of all EXPENSE transactions)

Get total of all income:

```bash
curl -X GET "http://localhost:8080/v1/transactions/summary/income" | jq
```

**Expected Response**: `3500.00` (sum of all INCOME transactions)

### 8. Filter by Transaction Stability

Get all fixed transactions:

```bash
curl -X GET "http://localhost:8080/v1/transactions?stability=FIXED" | jq
```

Get all variable transactions:

```bash
curl -X GET "http://localhost:8080/v1/transactions?stability=VARIABLE" | jq
```

### 9. Get Transaction by ID

Retrieve a specific transaction:

```bash
TRANSACTION_ID="a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
curl -X GET "http://localhost:8080/v1/transactions/${TRANSACTION_ID}" | jq
```

### 10. Update Transaction

Update transaction details (requires version for optimistic locking):

```bash
TRANSACTION_ID="a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
curl -X PUT "http://localhost:8080/v1/transactions/${TRANSACTION_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Groceries and household items",
    "amount": 52.40,
    "currency": "EUR",
    "date": "2026-01-19T10:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE",
    "categoryId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 0
  }' | jq
```

**Expected Response** (200 OK):
```json
{
  "id": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "description": "Groceries and household items",
  "amount": 52.40,
  "currency": "EUR",
  "date": "2026-01-19T10:00:00Z",
  "transactionType": "EXPENSE",
  "transactionStability": "VARIABLE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440000",
  "categoryName": "Supermarket",
  "version": 1,
  "createdAt": "2026-01-19T10:30:00Z",
  "updatedAt": "2026-01-19T11:00:00Z"
}
```

**Note**: `version` incremented from 0 to 1.

### 11. Delete Transaction

Permanently delete a transaction:

```bash
TRANSACTION_ID="a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
curl -X DELETE "http://localhost:8080/v1/transactions/${TRANSACTION_ID}" -v
```

**Expected Response**: 204 No Content

---

## Validation Error Examples

### Negative Amount

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": -30.75,
    "currency": "EUR",
    "date": "2026-01-19T10:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE"
  }' | jq
```

**Expected Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "amount",
      "rejectedValue": -30.75,
      "message": "Amount must be greater than zero"
    }
  ],
  "timestamp": "2026-01-19T11:00:00Z"
}
```

### Future Date

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": 30.75,
    "currency": "EUR",
    "date": "2027-01-01T00:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE"
  }' | jq
```

**Expected Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "date",
      "rejectedValue": "2027-01-01T00:00:00Z",
      "message": "Date cannot be in the future"
    }
  ],
  "timestamp": "2026-01-19T11:00:00Z"
}
```

### Invalid Currency Code

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test",
    "amount": 30.75,
    "currency": "INVALID",
    "date": "2026-01-19T10:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE"
  }' | jq
```

**Expected Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "currency",
      "rejectedValue": "INVALID",
      "message": "Invalid currency code. Must be ISO 4217 format (e.g., EUR, USD)"
    }
  ],
  "timestamp": "2026-01-19T11:00:00Z"
}
```

### Missing Required Fields

```bash
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Test"
  }' | jq
```

**Expected Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "amount",
      "message": "Amount is required"
    },
    {
      "field": "currency",
      "message": "Currency is required"
    },
    {
      "field": "date",
      "message": "Date is required"
    }
  ],
  "timestamp": "2026-01-19T11:00:00Z"
}
```

**Note**: `transactionType`, `transactionStability`, and `categoryId` are optional. They default to EXPENSE, VARIABLE, and "Others" category respectively.

### Optimistic Lock Conflict

Update with stale version:

```bash
TRANSACTION_ID="a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
curl -X PUT "http://localhost:8080/v1/transactions/${TRANSACTION_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated",
    "amount": 100.00,
    "currency": "EUR",
    "date": "2026-01-19T10:00:00Z",
    "transactionType": "EXPENSE",
    "transactionStability": "VARIABLE",
    "categoryId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 0
  }' | jq
```

**Expected Response** (409 Conflict) if version is outdated:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Transaction was modified by another user. Please refresh and try again.",
  "timestamp": "2026-01-19T11:00:00Z"
}
```

---

## Complete Workflow Example

### Scenario: Track Monthly Expenses

```bash
# 1. Get "Food & Drinks" category ID
FOOD_CATEGORY=$(curl -s http://localhost:8080/v1/categories | jq -r '.[] | select(.name=="Food & Drinks") | .id')

# 2. Record grocery expense (variable)
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d "{
    \"description\": \"Continente Groceries\",
    \"amount\": 65.40,
    \"currency\": \"EUR\",
    \"date\": \"2026-01-19T10:00:00Z\",
    \"transactionType\": \"EXPENSE\",
    \"transactionStability\": \"VARIABLE\",
    \"categoryId\": \"${FOOD_CATEGORY}\"
  }"

# 3. Record Netflix subscription (fixed)
SUBSCRIPTION_CATEGORY=$(curl -s http://localhost:8080/v1/categories | jq -r '.[] | select(.name=="Subscriptions") | .id')

curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d "{
    \"description\": \"Netflix Monthly\",
    \"amount\": 12.99,
    \"currency\": \"EUR\",
    \"date\": \"2026-01-01T00:00:00Z\",
    \"transactionType\": \"EXPENSE\",
    \"transactionStability\": \"FIXED\",
    \"categoryId\": \"${SUBSCRIPTION_CATEGORY}\"
  }"

# 4. Record salary (fixed income)
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Monthly Salary Deposit",
    "amount": 3000.00,
    "currency": "EUR",
    "date": "2026-01-01T00:00:00Z",
    "transactionType": "INCOME",
    "transactionStability": "FIXED"
  }'

# 5. View all transactions
curl -s http://localhost:8080/v1/transactions | jq

# 6. Filter only fixed transactions (salary + subscription)
curl -s "http://localhost:8080/v1/transactions?stability=FIXED" | jq

# 7. Filter only variable expenses
curl -s "http://localhost:8080/v1/transactions?stability=VARIABLE" | jq
```

---

## Testing H2 Console (Optional)

Access H2 database console at: `http://localhost:8080/h2-console`

**Connection Details**:
- JDBC URL: `jdbc:h2:file:./data/moneytrak`
- User: `sa`
- Password: (leave empty)

**Sample Queries**:
```sql
-- View all categories
SELECT * FROM categories ORDER BY name;

-- View all transactions with category names
SELECT
    t.id,
    t.description,
    t.amount,
    t.currency,
    t.transaction_type,
    t.transaction_stability,
    c.name as category_name
FROM transactions t
JOIN categories c ON t.category_id = c.id
ORDER BY t.date DESC;

-- Count transactions by type and stability
SELECT
    transaction_type,
    transaction_stability,
    COUNT(*) as count,
    SUM(amount) as total
FROM transactions
GROUP BY transaction_type, transaction_stability;
```

---

## Troubleshooting

### 404 Not Found

**Cause**: Old `/v1/expenses` endpoints no longer exist after migration.

**Solution**: Use `/v1/transactions` endpoints instead.

### 400 Bad Request: Invalid Enum Value

**Error**: `Invalid enum value: expense`

**Cause**: Enum values are case-sensitive.

**Solution**: Use uppercase: `EXPENSE`, `INCOME`, `FIXED`, `VARIABLE`.

### 409 Conflict: Duplicate Category

**Cause**: Category name already exists (case-insensitive check).

**Solution**: Use a different name or update the existing category.

### 409 Conflict: Cannot Delete Category

**Cause**: Category has associated transactions.

**Solution**: Reassign transactions to different category first, then delete.

---

## Next Steps

1. **Create custom categories** for your specific needs
2. **Import existing expense data** via bulk import script
3. **Set up recurring transactions** for fixed expenses/income
4. **Generate reports** by filtering transactions by type and stability
5. **Integrate with frontend** using the OpenAPI spec