-- Migration: Create accounts and readings tables
-- Feature: 004-portfolio-readings
-- Description: Creates accounts and readings tables for portfolio tracking with optimized indexes

-- ============================================================================
-- Create accounts table
-- ============================================================================

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- ============================================================================
-- Create readings table
-- ============================================================================

CREATE TABLE readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    amount DECIMAL(15, 8) NOT NULL,
    reading_date TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reading_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT
);

-- ============================================================================
-- Create performance indexes
-- ============================================================================

-- Deterministic ordering for accounts (name + id for duplicate names)
CREATE INDEX idx_accounts_name ON accounts(name, id);

-- Optimized lookup for latest readings per account (DESC for most recent first)
CREATE INDEX idx_readings_account_date ON readings(account_id, reading_date DESC);

-- Fast filtering of soft-deleted readings
CREATE INDEX idx_readings_deleted ON readings(deleted);
