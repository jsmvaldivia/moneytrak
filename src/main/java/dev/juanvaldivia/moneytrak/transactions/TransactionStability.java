package dev.juanvaldivia.moneytrak.transactions;

/**
 * Enumeration representing the stability/predictability of a transaction.
 * Classifies transactions as recurring/predictable (FIXED) or one-time/variable (VARIABLE).
 *
 * <p>This classification applies to both INCOME and EXPENSE transaction types,
 * enabling better budget planning and cash flow prediction.</p>
 */
public enum TransactionStability {
    /**
     * Recurring, predictable transactions (e.g., salary, subscriptions, rent, insurance).
     * Used for budget planning and regular cash flow projections.
     */
    FIXED,

    /**
     * One-time, unpredictable transactions (e.g., groceries, gas, freelance payments, bonuses).
     * Default value when not specified. Used for irregular expenses and income.
     */
    VARIABLE
}
