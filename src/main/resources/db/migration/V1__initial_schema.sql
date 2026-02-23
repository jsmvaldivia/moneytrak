-- Migration: Initial schema for MoneyTrak
-- Description: Creates categories and transactions tables with all required columns

-- ============================================================================
-- Create categories table
-- ============================================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- ============================================================================
-- Insert 15 predefined categories
-- ============================================================================

INSERT INTO categories (id, name, is_predefined, version, created_at, updated_at) VALUES
    (gen_random_uuid(), 'Office Renting', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Public Transport', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Bank', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Car Maintenance', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Food & Drinks', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Subscriptions', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Supermarket', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Tolls', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Gas', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Sport', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Gifts', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'ATM', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Video & Films', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Transfers', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Others', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- Create transactions table
-- ============================================================================

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(11, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL,
    transaction_type VARCHAR(32) NOT NULL DEFAULT 'EXPENSE',
    transaction_stability VARCHAR(32) NOT NULL DEFAULT 'VARIABLE',
    category_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES categories(id)
);
