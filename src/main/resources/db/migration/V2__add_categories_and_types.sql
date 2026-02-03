-- Migration: Add transaction categories and types
-- Feature: 002-transaction-categories
-- Description: Creates categories table, renames expenses to transactions, adds transaction type and stability fields

-- ============================================================================
-- Phase 1: Create categories table
-- ============================================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create index for name lookups (case-insensitive queries handled at application level)
CREATE INDEX idx_categories_name ON categories(name);

-- ============================================================================
-- Phase 2: Insert 14 predefined categories
-- ============================================================================

INSERT INTO categories (id, name, is_predefined, version, created_at, updated_at) VALUES
    (RANDOM_UUID(), 'Office Renting', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Public Transport', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Bank', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Car Maintenance', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Food & Drinks', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Subscriptions', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Supermarket', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Tolls', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Gas', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Sport', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Gifts', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'ATM', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Video & Films', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Transfers', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'Others', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- Phase 3: Rename expenses table to transactions and add new columns
-- ============================================================================

-- Rename table (if expenses table exists from V1 migration)
ALTER TABLE IF EXISTS expenses RENAME TO transactions;

-- Add new columns for transaction type and stability (nullable initially for data migration)
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS transaction_type VARCHAR(32);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS transaction_stability VARCHAR(32);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS category_id UUID;

-- ============================================================================
-- Phase 4: Data migration - set default values for existing records
-- ============================================================================

-- Set default transaction type to EXPENSE for all existing records
UPDATE transactions
SET transaction_type = 'EXPENSE'
WHERE transaction_type IS NULL;

-- Set default transaction stability to VARIABLE for all existing records
UPDATE transactions
SET transaction_stability = 'VARIABLE'
WHERE transaction_stability IS NULL;

-- Set default category to "Others" for all existing records
UPDATE transactions
SET category_id = (SELECT id FROM categories WHERE name = 'Others' AND is_predefined = TRUE LIMIT 1)
WHERE category_id IS NULL;

-- ============================================================================
-- Phase 5: Add constraints after data migration
-- ============================================================================

-- Make new columns non-nullable
ALTER TABLE transactions ALTER COLUMN transaction_type SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN transaction_stability SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN category_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE transactions ADD CONSTRAINT fk_transaction_category
    FOREIGN KEY (category_id) REFERENCES categories(id);

-- ============================================================================
-- Phase 6: Create indexes for query optimization
-- ============================================================================

CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_stability ON transactions(transaction_stability);
CREATE INDEX idx_transactions_date ON transactions(date DESC);
