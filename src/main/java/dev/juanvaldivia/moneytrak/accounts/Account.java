package dev.juanvaldivia.moneytrak.accounts;

import jakarta.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity representing a financial account.
 * Tracks account metadata for portfolio holdings (banks, brokers, crypto exchanges, etc.).
 *
 * <p>Subject to a hard limit of 1000 accounts per system enforced at the service layer.</p>
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountType type;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    protected Account() {
        // JPA requires no-arg constructor
    }

    private Account(
        UUID id,
        String name,
        AccountType type,
        String currency,
        Integer version,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new account.
     *
     * @param name account name
     * @param type account type (BANK, BROKER, STOCK, P2P, CRYPTO, OTHER)
     * @param currency ISO 4217 currency code
     * @return new account instance
     */
    public static Account create(String name, AccountType type, String currency) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return new Account(null, name, type, currency, 0, now, now);
    }

    /**
     * Update account fields.
     *
     * @param name new account name
     * @param type new account type
     * @param currency new currency code
     */
    public void update(String name, AccountType type, String currency) {
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    // Getters
    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public AccountType type() {
        return type;
    }

    public String currency() {
        return currency;
    }

    public Integer version() {
        return version;
    }

    public ZonedDateTime createdAt() {
        return createdAt;
    }

    public ZonedDateTime updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
