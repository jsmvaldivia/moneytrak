package dev.juanvaldivia.moneytrak.accounts;

/**
 * Enumeration representing the type of financial account.
 * Categorizes accounts based on their purpose and financial institution type.
 */
public enum AccountType {
    /**
     * Traditional bank account (checking, savings, etc.).
     */
    BANK,

    /**
     * Brokerage account for trading securities.
     */
    BROKER,

    /**
     * Direct stock ownership account.
     */
    STOCK,

    /**
     * Peer-to-peer lending platform account.
     */
    P2P,

    /**
     * Cryptocurrency exchange or wallet.
     */
    CRYPTO,

    /**
     * Other account types not covered by specific categories.
     */
    OTHER
}
