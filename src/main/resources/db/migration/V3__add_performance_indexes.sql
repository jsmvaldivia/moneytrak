-- Migration: Add performance indexes
-- Feature: review/performance-improvements
-- Description: Adds composite index on (transaction_type, amount) to support efficient aggregate queries

-- Composite index enables index-only scans for SUM(amount) WHERE transaction_type = ? queries
CREATE INDEX idx_transactions_type_amount ON transactions(transaction_type, amount);
