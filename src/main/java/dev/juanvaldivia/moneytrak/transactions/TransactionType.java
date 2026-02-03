package dev.juanvaldivia.moneytrak.transactions;

/**
 * Enumeration representing the type of financial transaction.
 * Distinguishes between money coming in (income) and money going out (expenses).
 *
 * <p>Amounts are always stored as positive values; the transaction type provides
 * the semantic meaning of the transaction direction.</p>
 */
public enum TransactionType {
    /**
     * Money going out (e.g., groceries, rent, subscriptions).
     */
    EXPENSE,

    /**
     * Money coming in (e.g., salary, refunds, gifts).
     */
    INCOME
}
